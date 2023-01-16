package planet;

import java.util.HashMap;

public interface ServerCommandsListener
{
    void s2cInit(int width, int height);

    void s2cLanded(Ground ground, double temp);

    void s2cScanned(HashMap data);

    void s2cMoved(HashMap data);

    void s2cRotated(HashMap data);

    void s2cCrashed(HashMap data);

    void s2cError(HashMap data);

    void s2cPos(HashMap data);

    void s2cCharged(HashMap data);

    void s2cStatus(HashMap data);

}

