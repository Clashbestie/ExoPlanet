package org.example.position;

import com.google.gson.annotations.SerializedName;


public class Position
{
    @SerializedName(value = "X")
    private int x;
    @SerializedName(value = "Y")
    private int y;
    @SerializedName(value = "DIRECTION")
    private Direction direction;

    public Position(int x, int y, Direction direction)
    {
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    public int getX()
    {
        return x;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public int getY()
    {
        return y;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public Direction getDir()
    {
        return direction;
    }

    public void setDir(Direction direction)
    {
        this.direction = direction;
    }
}
