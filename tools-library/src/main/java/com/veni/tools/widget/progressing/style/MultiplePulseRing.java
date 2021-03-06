package com.veni.tools.widget.progressing.style;


import com.veni.tools.widget.progressing.sprite.Sprite;
import com.veni.tools.widget.progressing.sprite.SpriteContainer;

/**
 * Created by ybq.
 * 复杂环形脉冲
 */
public class MultiplePulseRing extends SpriteContainer {

    @Override
    public Sprite[] onCreateChild() {
        return new Sprite[]{
                new PulseRing(),
                new PulseRing(),
                new PulseRing(),
        };
    }

    @Override
    public void onChildCreated(Sprite... sprites) {
        for (int i = 0; i < sprites.length; i++) {
            sprites[i].setAnimationDelay(200 * (i + 1));
        }
    }
}
