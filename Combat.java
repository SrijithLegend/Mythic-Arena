import java.sql.*;
import java.util.Random;
import java.util.Scanner;

public class Combat {

    public static class Enemy {
        String name;
        String specialty;
        int hp, maxHp, attack, defense, magicAttack, magicDefense, speed;
        String[] moves = new String[4];
    }

    public static void startBattle(Scanner scanner, String targetName) {
        Enemy enemy = loadEnemy(targetName);
        
        if (enemy == null) {
            System.out.println("\n[!] Could not find fighter '" + targetName + "' in the database. Returning to Arena.");
            return;
        }

        System.out.println("\n⚔️ ======================================== ⚔️");
        System.out.println("      BATTLE START: " + Player.name + " VS " + enemy.name);
        System.out.println("⚔️ ======================================== ⚔️\n");

        int playerHp = Player.setPlayerstats.hp;
        int playerMaxHp = Player.setPlayerstats.hp;
        int playerAttack = Player.setPlayerstats.attack;
        int playerDefense = Player.setPlayerstats.defense;
        int playerMagicAttack = Player.setPlayerstats.magicAttack;
        int playerMagicDefense = Player.setPlayerstats.magicDefense;
        int playerSpeed = Player.setPlayerstats.speed;
        
        Random rand = new Random();

        while (playerHp > 0 && enemy.hp > 0) {
            
            if (playerSpeed >= enemy.speed) {
                enemy.hp = playerTurn(scanner, playerHp, playerMaxHp, enemy);
                if (enemy.hp <= 0) break;
                
                playerHp = enemyTurn(playerHp, enemy, rand);
            } else {
                playerHp = enemyTurn(playerHp, enemy, rand);
                if (playerHp <= 0) break;
                
                enemy.hp = playerTurn(scanner, playerHp, playerMaxHp, enemy);
            }
        }

        // BATTLE RESULTS
        if (playerHp > 0) {
            System.out.println("\n🏆 YOU DEFEATED " + enemy.name.toUpperCase() + "! 🏆");
            System.out.println("You proved your strength in the Arena.");
        } else {
            System.out.println("\n☠️ You were struck down by " + enemy.name + "... ☠️");
        }
    }

    private static int playerTurn(Scanner scanner, int playerHp, int playerMaxHp, Enemy enemy) {
        System.out.println("\n--- YOUR TURN ---");
        System.out.println("Your HP: " + playerHp + "/" + playerMaxHp + "  |  " + enemy.name + "'s HP: " + enemy.hp + "/" + enemy.maxHp);
        System.out.println("Choose your move:");
        
        for (int i = 0; i < Moves.selectedMoves.length; i++) {
            System.out.println("  " + (i + 1) + ". " + Moves.selectedMoves[i].description());
        }

        int choice = -1;
        while (choice < 1 || choice > 4) {
            System.out.print("Select a move (1-4): ");
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine();
            } else {
                scanner.nextLine();
            }
        }

        int damage = Math.max(1, Player.setPlayerstats.attack - (enemy.defense / 2));
        
        System.out.println("\n💥 You strike with a powerful attack and deal " + damage + " damage!");
        return enemy.hp - damage;
    }

    private static int enemyTurn(int playerHp, Enemy enemy, Random rand) {
        System.out.println("\n--- ENEMY TURN ---");
        
        int moveIndex = rand.nextInt(4); 
        String usedMove = enemy.moves[moveIndex];

        String moveName = usedMove.split(":")[0].trim();

        int damage = Math.max(1, enemy.attack - (Player.setPlayerstats.defense / 2));
        
        System.out.println("⚔️ " + enemy.name + " uses [" + moveName + "] and deals " + damage + " damage!");
        return playerHp - damage;
    }

    private static Enemy loadEnemy(String searchName) {
        String url = "jdbc:sqlite:game_data.db";
        String query = "SELECT * FROM player_stats WHERE name = ? ORDER BY level DESC LIMIT 1";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, searchName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Enemy e = new Enemy();
                e.name = rs.getString("name");
                e.specialty = rs.getString("specialty");
                e.maxHp = rs.getInt("hp");
                e.hp = e.maxHp;
                e.attack = rs.getInt("attack");
                e.defense = rs.getInt("defense");
                e.magicAttack = rs.getInt("magicAttack");
                e.magicDefense = rs.getInt("magicDefense");
                e.speed = rs.getInt("speed");
                e.moves[0] = rs.getString("move1");
                e.moves[1] = rs.getString("move2");
                e.moves[2] = rs.getString("move3");
                e.moves[3] = rs.getString("move4");
                return e;
            }
        } catch (SQLException e) {
            System.out.println("Error loading opponent data!");
        }
        return null;
    }
}