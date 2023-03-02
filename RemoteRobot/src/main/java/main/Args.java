package main;

public class Args {

    public static final int normalMinTemp = -50;
    public static final int normalMaxTemp = 150;
    public static final int crashMinTemp = -120;
    public static final int crashMaxTemp = 280;

    private static boolean debug = false;
    private static boolean noAuto = false;
    private static boolean landOnly = false;
    private static String name = "test";
    public static boolean getDebug() {
        return debug;
    }
    public static boolean getNoAuto() {
        return noAuto;
    }
    public static boolean getLandOnly() {
        return landOnly;
    }
    public static String getName() {
        return name;
    }
    public static void setName(String name){ Args.name = name;}

    public static void readArgs(String[] args){
        for (String arg : args) {
            if (arg.equals("debug")) debug = true;
            if (arg.equals("noAuto")) noAuto = true;
            if (arg.equals("landOnly")) landOnly = true;
            if (arg.startsWith("name:")) name = arg.split(":")[1];
        }
    }
}
