package planet;

import java.lang.reflect.Field;

public enum Ground {
    NOTHING("NICHTS"),
    SAND(""),
    GRAVEL("GEROELL"),
    ROCK("FELS"),
    WATER("WASSER"),
    PLANT("PFLANZEN"),
    MORASS("MORAST"),
    LAVA(""),
    OOB("");

    Ground(String name) {
        if (name.isEmpty()) return;
        //Overriding name to keep #valueOf working
        try {
            Field fieldName = getClass().getSuperclass().getDeclaredField("name");
            fieldName.setAccessible(true);
            fieldName.set(this, name);
            fieldName.setAccessible(false);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            if (e.getClass().equals(NoSuchFieldException.class))
                System.out.println("MOM! Phineas and Ferb are messing with my ENUMS again!");
            else System.out.println("PLS GIVE REFLECTION.");
        }
    }
}
