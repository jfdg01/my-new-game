package com.kandclay.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.spine.*;
import com.esotericsoftware.spine.attachments.RegionAttachment;
import com.kandclay.Main;
import com.kandclay.managers.MyAssetManager;
import com.kandclay.utils.TrailDot;
import de.eskalon.commons.screen.ManagedScreen;

import java.util.HashMap;

public abstract class BaseScreen extends ManagedScreen {

    protected Main game;
    protected ShapeRenderer shapeRenderer;
    protected MyAssetManager assetManager;

    protected SkeletonRenderer skeletonRenderer;
    protected Array<AnimationState> states;
    protected Array<Skeleton> skeletons;
    protected HashMap<String, Boolean> hoverStates;

    public BaseScreen() {
        game = (Main) Gdx.app.getApplicationListener();
        this.skeletons = new Array<Skeleton>();
        this.states = new Array<AnimationState>();

        TrailDot.setSpineAnimationHandler(game.getSpineAnimationHandler());
    }

    public void addTrailToStage(final Stage stage, final Viewport viewport) {
        stage.addListener(new InputListener() {
            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                TrailDot.createTrailDot(x, y, viewport);
                return false;
            }
        });
    }

    @Override
    public void show() {

    }

    public void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    public void clearScreen(int red, int green, int blue, int alpha) {
        Gdx.gl.glClearColor(red, green, blue, alpha);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void render(float delta) {
        clearScreen();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {
        // Override in subclasses if needed
    }

    @Override
    public void resume() {
        // Override in subclasses if needed
    }

    @Override
    public void hide() {
        // Override in subclasses to hide the screen
    }

    @Override
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }

    protected void setSkeletonScale(Skeleton skeleton, float widthPercentage, float heightPercentage, Viewport viewport) {
        if (skeleton != null) {
            float screenWidth = viewport.getWorldWidth();
            float screenHeight = viewport.getWorldHeight();

            float skeletonWidth = screenWidth * widthPercentage;
            float skeletonHeight = screenHeight * heightPercentage;

            float scaleX = skeletonWidth / skeleton.getData().getWidth();
            float scaleY = skeletonHeight / skeleton.getData().getHeight();

            float scale = Math.min(scaleX, scaleY);

            skeleton.setScale(scale, scale);
        }
    }

    protected boolean isHoveringButton(float x, float y, String buttonName, int pos) {
        Rectangle buttonBounds = getButtonBounds(buttonName, pos);
        return buttonBounds.contains(x, y);
    }

    protected Rectangle getButtonBounds(String buttonName, int pos) {
        return getRectangle(buttonName, buttonName, skeletons.get(pos));
    }

    protected void updateHoverState(float x, float y, String buttonName, int pos, int trackIndex, String hoverInAnim, String hoverOutAnim) {
        boolean isHovered = isHoveringButton(x, y, buttonName, pos);
        boolean wasHovered = hoverStates.get(buttonName);

        if (isHovered && !wasHovered) {
            states.get(pos).setAnimation(trackIndex, hoverInAnim, false);
        } else if (!isHovered && wasHovered) {
            states.get(pos).setAnimation(trackIndex, hoverOutAnim, false);
        }

        hoverStates.put(buttonName, isHovered);
    }

    protected Rectangle getRectangle(String buttonName, String bgSlotName, Skeleton skeleton) {
        Bone bone = skeleton.findBone(buttonName);
        if (bone == null) return new Rectangle();

        Slot slot = skeleton.findSlot(bgSlotName);  // Use the background slot
        if (slot == null || !(slot.getAttachment() instanceof RegionAttachment)) return new Rectangle();

        RegionAttachment attachment = (RegionAttachment) slot.getAttachment();
        if (attachment == null) return new Rectangle();

        float[] vertices = new float[8];
        attachment.computeWorldVertices(slot.getBone(), vertices, 0, 2);

        float minX = vertices[0];
        float minY = vertices[1];
        float maxX = vertices[0];
        float maxY = vertices[1];

        for (int i = 2; i < vertices.length; i += 2) {
            if (vertices[i] < minX) minX = vertices[i];
            if (vertices[i + 1] < minY) minY = vertices[i + 1];
            if (vertices[i] > maxX) maxX = vertices[i];
            if (vertices[i + 1] > maxY) maxY = vertices[i + 1];
        }

        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    protected void setSkeletonPosition(Skeleton skeleton, float x, float y) {
        if (skeleton != null) {
            skeleton.setPosition(x, y);
        }
    }
}

