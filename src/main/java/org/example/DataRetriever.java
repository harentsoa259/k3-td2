package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
    private final DBConnection db = new DBConnection();

    public Team findTeamById(Integer id) {
        String sqlTeam = "SELECT * FROM Team WHERE id = ?";
        String sqlPlayers = "SELECT * FROM Player WHERE id_team = ?";
        try (Connection conn = db.getDBConnection();
             PreparedStatement psT = conn.prepareStatement(sqlTeam);
             PreparedStatement psP = conn.prepareStatement(sqlPlayers)) {

            psT.setInt(1, id);
            ResultSet rsT = psT.executeQuery();
            if (rsT.next()) {
                Team team = new Team(
                        rsT.getInt("id"),
                        rsT.getString("name"),
                        ContinentEnum.valueOf(rsT.getString("continent").toUpperCase())
                );

                psP.setInt(1, id);
                ResultSet rsP = psP.executeQuery();
                while (rsP.next()) {
                    team.addPlayer(new Player(
                            rsP.getInt("id"),
                            rsP.getString("name"),
                            rsP.getInt("age"),
                            PlayerPositionEnum.valueOf(rsP.getString("position").toUpperCase()),
                            team
                    ));
                }
                return team;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findTeamById : " + e.getMessage());
        }
        return null;
    }

    public List<Player> findPlayers(int page, int size) {
        List<Player> players = new ArrayList<>();
        String sql = "SELECT * FROM Player LIMIT ? OFFSET ?";
        try (Connection conn = db.getDBConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, size);
            ps.setInt(2, (page - 1) * size);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                players.add(new Player(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        PlayerPositionEnum.valueOf(rs.getString("position").toUpperCase()),
                        null
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findPlayers : " + e.getMessage());
        }
        return players;
    }

    public List<Player> createPlayers(List<Player> newPlayers) {
        try (Connection conn = db.getDBConnection()) {
            conn.setAutoCommit(false);
            // CORRECTION : Utilisation de position_type selon votre erreur SQL
            String sqlIns = "INSERT INTO Player (id, name, age, position) VALUES (?, ?, ?, ?::position_type)";
            try (PreparedStatement psCheck = conn.prepareStatement("SELECT 1 FROM Player WHERE id = ?");
                 PreparedStatement psIns = conn.prepareStatement(sqlIns)) {

                for (Player p : newPlayers) {
                    psCheck.setInt(1, p.getId());
                    if (psCheck.executeQuery().next()) {
                        conn.rollback();
                        throw new RuntimeException("Doublon détecté pour l'ID: " + p.getId());
                    }
                    psIns.setInt(1, p.getId());
                    psIns.setString(2, p.getName());
                    psIns.setInt(3, p.getAge());
                    psIns.setString(4, p.getPosition().name());
                    psIns.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Erreur lors de l'insertion : " + e.getMessage());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return newPlayers;
    }

    public Team saveTeam(Team teamToSave) {
        String upsert = "INSERT INTO Team (id, name, continent) VALUES (?, ?, ?::continent_type) " +
                "ON CONFLICT (id) DO UPDATE SET name=EXCLUDED.name, continent=EXCLUDED.continent";
        try (Connection conn = db.getDBConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(upsert)) {
                ps.setInt(1, teamToSave.getId());
                ps.setString(2, teamToSave.getName());
                ps.setString(3, teamToSave.getContinent().name());
                ps.executeUpdate();

                try (PreparedStatement psDetach = conn.prepareStatement("UPDATE Player SET id_team = NULL WHERE id_team = ?")) {
                    psDetach.setInt(1, teamToSave.getId());
                    psDetach.executeUpdate();
                }

                if (teamToSave.getPlayers() != null) {
                    try (PreparedStatement psAttach = conn.prepareStatement("UPDATE Player SET id_team = ? WHERE id = ?")) {
                        for (Player p : teamToSave.getPlayers()) {
                            psAttach.setInt(1, teamToSave.getId());
                            psAttach.setInt(2, p.getId());
                            psAttach.executeUpdate();
                        }
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return teamToSave;
    }

    public List<Team> findTeamsByPlayerName(String playerName) {
        List<Team> teams = new ArrayList<>();
        String sql = "SELECT DISTINCT t.* FROM Team t JOIN Player p ON t.id = p.id_team WHERE p.name ILIKE ?";
        try (Connection conn = db.getDBConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + playerName + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                teams.add(new Team(
                        rs.getInt("id"),
                        rs.getString("name"),
                        ContinentEnum.valueOf(rs.getString("continent").toUpperCase())
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return teams;
    }

    public List<Player> findPlayersByCriteria(String name, PlayerPositionEnum pos, String team, ContinentEnum cont, int pg, int sz) {
        List<Player> players = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT p.* FROM Player p LEFT JOIN Team t ON p.id_team = t.id WHERE 1=1");

        if (name != null) sql.append(" AND p.name ILIKE ?");
        if (pos != null) sql.append(" AND p.position = ?::position_type");
        if (team != null) sql.append(" AND t.name ILIKE ?");
        if (cont != null) sql.append(" AND t.continent = ?::continent_type");

        sql.append(" LIMIT ? OFFSET ?");

        try (Connection conn = db.getDBConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i = 1;
            if (name != null) ps.setString(i++, "%" + name + "%");
            if (pos != null) ps.setString(i++, pos.name());
            if (team != null) ps.setString(i++, "%" + team + "%");
            if (cont != null) ps.setString(i++, cont.name());

            ps.setInt(i++, sz);
            ps.setInt(i, (pg - 1) * sz);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                players.add(new Player(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        PlayerPositionEnum.valueOf(rs.getString("position").toUpperCase()),
                        null
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return players;
    }
}