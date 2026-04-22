public class MoonCycle {
    public enum Phase {
        NEW, WAXING, FULL, WANING
    }

    private Phase currentPhase;
    private int moves; // counts player moves
    private int movesToNextPhase; // random between 5-10

    public MoonCycle() {
        currentPhase = Phase.NEW;
        moves = 0;
        movesToNextPhase = randomMoves();
    }

    private int randomMoves() {
        return 5 + (int)(Math.random() * 6); // 5-10 moves
    }

    // Call this every time the player moves
    public void playerMoved() {
        moves++;
        if (moves >= movesToNextPhase) {
            nextPhase();
        }
    }

    private void nextPhase() {
        moves = 0;
        movesToNextPhase = randomMoves();

        switch (currentPhase) {
            case NEW: currentPhase = Phase.WAXING; break;
            case WAXING: currentPhase = Phase.FULL; break;
            case FULL: currentPhase = Phase.WANING; break;
            case WANING: currentPhase = Phase.NEW; break;
        }

        System.out.println("🌙 The moon is now " + currentPhase);
    }

    public Phase getCurrentPhase() {
        return currentPhase;
    }
}