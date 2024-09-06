package com.kandclay.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.spine.*;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.attachments.MeshAttachment;
import com.esotericsoftware.spine.attachments.RegionAttachment;
import com.kandclay.Main;
import com.kandclay.managers.MyAssetManager;
import com.kandclay.utils.TrailDot;
import de.eskalon.commons.screen.ManagedScreen;

import java.security.interfaces.RSAKey;
import java.util.HashMap;

public abstract class BaseScreen extends ManagedScreen {

    protected Main game;
    protected ShapeRenderer shapeRenderer;
    protected MyAssetManager assetManager;

    protected SkeletonRenderer skeletonRenderer;
    protected Array<AnimationState> states;
    protected Array<Skeleton> skeletons;
    protected HashMap<String, Boolean> hoverStates;

    protected Vector2 lastTouchPosition;
    protected boolean isDragging;

    protected Rectangle skeletonBounds;
    protected Vector2 tempPosition;
    protected Vector2 tempSize;
    protected FloatArray tempVertices;

    public BaseScreen() {
        game = (Main) Gdx.app.getApplicationListener();
        this.skeletons = new Array<Skeleton>();
        this.states = new Array<AnimationState>();

        TrailDot.setSpineAnimationHandler(game.getSpineAnimationHandler());

        lastTouchPosition = new Vector2();
        skeletonBounds = new Rectangle();
        tempPosition = new Vector2();
        tempSize = new Vector2();
        tempVertices = new FloatArray();
    }

    public void setupSkeletonDragging(Stage stage) {
        stage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (isOverSkeleton(x, y)) {
                    isDragging = true;
                    lastTouchPosition.set(x, y);
                    return true;
                }
                return false;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if (isDragging) {
                    float deltaX = x - lastTouchPosition.x;
                    float deltaY = y - lastTouchPosition.y;
                    if (!skeletons.isEmpty()) {
                        Skeleton skeleton = skeletons.get(0);  // Assuming you want to drag the first skeleton
                        skeleton.setPosition(skeleton.getX() + deltaX, skeleton.getY() + deltaY);
                        lastTouchPosition.set(x, y);
                    }
                }
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                isDragging = false;
            }
        });
    }

    protected void updateSkeletonBounds() {
        if (!skeletons.isEmpty()) {
            Skeleton skeleton = skeletons.get(0);  // Assuming the first skeleton is the one being interacted with
            skeleton.getBounds(tempPosition, tempSize, tempVertices);
            skeletonBounds.set(tempPosition.x, tempPosition.y, tempSize.x, tempSize.y);
        }
    }

    protected boolean isOverSkeleton(float x, float y) {
        if (skeletonBounds.contains(x, y) && !skeletons.isEmpty()) {
            Skeleton skeleton = skeletons.get(0);
            for (Slot slot : skeleton.getSlots()) {
                Attachment attachment = slot.getAttachment();
                if (attachment instanceof RegionAttachment) {
                    RegionAttachment region = (RegionAttachment) attachment;
                    float[] vertices = tempVertices.setSize(8);
                    region.computeWorldVertices(slot.getBone(), vertices, 0, 2);
                    if (isPointInPolygon(x, y, vertices, 8)) {
                        return true;
                    }
                } else if (attachment instanceof MeshAttachment) {
                    MeshAttachment mesh = (MeshAttachment) attachment;
                    int vertexCount = mesh.getWorldVerticesLength();
                    float[] vertices = tempVertices.setSize(vertexCount);
                    mesh.computeWorldVertices(slot, 0, vertexCount, vertices, 0, 2);
                    if (isPointInPolygon(x, y, vertices, vertexCount)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isPointInPolygon(float x, float y, float[] vertices, int vertexCount) {
        boolean inside = false;
        for (int i = 0, j = vertexCount - 2; i < vertexCount; j = i, i += 2) {
            float xi = vertices[i], yi = vertices[i + 1];
            float xj = vertices[j], yj = vertices[j + 1];
            if (((yi > y) != (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi)) {
                inside = !inside;
            }
        }
        return inside;
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
        clearScreen(0, 0, 0, 0);
    }

    public void clearScreen(float red, float green, float blue, float alpha) {
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

    protected void changeAttachmentColor(String slotName, Color color, Skeleton skeleton) {
        Slot slot = skeleton.findSlot(slotName);
        if (slot != null) {
            slot.getColor().set(color);
        } else {
            Gdx.app.log("DiamondScreen", "Slot not found: " + slotName);
        }
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

