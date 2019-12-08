package cubix.objects;

import edu.utc.game.*;
import org.lwjgl.opengl.GL11;

public class Button extends GameObject {
    private Text text;
    private Player.COLORS color;
    private static Texture texture = new Texture("res\\buttons.png");
    public Button(int x, int y, int size, Player.COLORS color,  String text)
    {
        this.hitbox.setBounds(Game.ui.getWidth()/2+32*x,Game.ui.getHeight()/2+32*y,64*size,32*size);
        int textSize = 10 * size;
        int xPos = Game.ui.getWidth()/2+32*x + (64*size-(text.length()*textSize/2+textSize))/2;
        this.text = new Text(xPos, Game.ui.getHeight()/2+32*y+12*size, textSize, textSize, text);
        this.color = color;
    }

    public boolean tryClick(int x, int y)
    {
        if (x >= this.hitbox.x && x <= this.hitbox.x + this.hitbox.width &&
            y >= this.hitbox.y && y <= this.hitbox.y + this.hitbox.height)
        {
            return true;
        }
        return false;
    }

    @Override
    public void draw() {
        float adjust = 0;
        if (color == Player.COLORS.RED)
            adjust = 0.5f;
        GL11.glColor3f(1,1,1);
        texture.bind();
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0f + adjust,0);
        GL11.glVertex2f(this.hitbox.x, this.hitbox.y);
        GL11.glTexCoord2f(0.5f + adjust,0);
        GL11.glVertex2f(this.hitbox.x+this.hitbox.width, this.hitbox.y);
        GL11.glTexCoord2f(0.5f + adjust,1);
        GL11.glVertex2f(this.hitbox.x+this.hitbox.width, this.hitbox.y+this.hitbox.height);
        GL11.glTexCoord2f(0f + adjust,1);
        GL11.glVertex2f(this.hitbox.x, this.hitbox.y+this.hitbox.height);
        GL11.glEnd();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D,  0);
        text.draw();
    }
}
