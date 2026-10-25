public class HappyHour {
    private boolean usedOnce = false;
    private int remainingTurns = 0;
    private final double discount = 0.20;

    public boolean activate() {
        if (this.usedOnce) {
            return false;
        }
        this.usedOnce = true;
        this.remainingTurns = 2;
        return true;
    }

    public boolean isActive() {
        return this.remainingTurns > 0;
    }

    public int getRemainingTurns() {
        return this.remainingTurns;
    }

    public boolean isUsedOnce() {
        return this.usedOnce;
    }

    public double getDiscount() {
        return this.discount;
    }

    public void advanceTurn() {
        if (this.remainingTurns > 0) {
            this.remainingTurns--;
        }
    }
}