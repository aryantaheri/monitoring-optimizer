package no.uis.ux.cipsi;

public class Play {

    public static void main(String[] args) {
        //        Double a = Math.pow(10, 9)*176;
        //        System.out.println(a.toString());
        boolOp();
    }

    private static void boolOp() {
        for (boolean onPath : new boolean[]{true, false}) {
            for (boolean es : new boolean[]{true, false}) {
                for (boolean eh : new boolean[]{true, false}) {
                    boolean res = onPath & !(es & eh);
                    System.out.println(onPath + " " + es + " " + eh + " " + res + " ");
                }
            }
        }
    }
}
