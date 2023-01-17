package planet;

import position.Direction;
import position.Position;

public interface ServerCommandsListener
{
    void s2cInit(int width, int height);

    void s2cLanded(Ground ground, double temp);

    void s2cScanned(Ground ground, double temp);

    void s2cMoved(Position position);

    void s2cRotated(Direction direction);

    void s2cCrashed();

    void s2cError(String text);

    void s2cPos(Position position);

    void s2cCharged(double temp, int energy, String text);

    void s2cStatus(double temp, int energy, String text);

}

