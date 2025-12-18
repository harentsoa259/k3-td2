package org.example;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        DataRetriever retriever = new DataRetriever();

        System.out.println("=== DÉBUT DU TEST SIMPLIFIÉ ===");

        try {
            System.out.println("\n[1] LECTURE DE L'ÉQUIPE 1");
            Team team1 = retriever.findTeamById(1);
            if (team1 != null) {
                System.out.println("-> OK : " + team1.getName() + " contient " + team1.getPlayers().size() + " joueur(s).");
            } else {
                System.out.println("-> ERREUR : L'équipe 1 n'existe pas dans la base SQL.");
            }

            System.out.println("\n[2] TEST PAGINATION (Page 1, Taille 2)");
            List<Player> players = retriever.findPlayers(1, 2);
            System.out.println("-> Joueurs trouvés : " + players.size());

            System.out.println("\n[3] CRÉATION DE NOUVEAUX JOUEURS (IDs 6 et 7)");
            try {
                Player p6 = new Player(6, "Vinicius", 23, PlayerPositionEnum.STR, null);
                Player p7 = new Player(7, "Pedri", 21, PlayerPositionEnum.MIDF, null);

                retriever.createPlayers(List.of(p6, p7));
                System.out.println("-> OK : Joueurs insérés avec succès.");
            } catch (Exception e) {
                System.out.println("-> INFO : Les joueurs existent déjà (ou erreur de doublon).");
            }

            System.out.println("\n[4] ASSOCIATION JOUEUR -> ÉQUIPE");
            if (team1 != null) {
                Player vini = new Player(6, "Vinicius", 23, PlayerPositionEnum.STR, team1);
                team1.addPlayer(vini);

                retriever.saveTeam(team1);
                System.out.println("-> OK : Vinicius est maintenant lié à " + team1.getName());
            }

        } catch (Exception e) {
            System.out.println("\n--- ERREUR DURANT LE TEST ---");
            System.out.println("Message : " + e.getMessage());
        }

        System.out.println("\n=== FIN DU TEST ===");
    }
}