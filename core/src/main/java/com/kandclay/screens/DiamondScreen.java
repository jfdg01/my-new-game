package com.kandclay.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.viewport.*;
import com.esotericsoftware.spine.*;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.attachments.BoundingBoxAttachment;
import com.esotericsoftware.spine.attachments.MeshAttachment;
import com.esotericsoftware.spine.attachments.RegionAttachment;
import com.kandclay.utils.Constants;

public class DiamondScreen extends BaseScreen {
    private Stage stage;
    private Skeleton skeleton;
    private AnimationState state;
    private SkeletonRenderer skeletonRenderer;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;
    private Vector2 lastTouchPosition;
    private boolean isDragging;

    private Rectangle skeletonBounds;
    private Vector2 tempPosition;
    private Vector2 tempSize;
    private FloatArray tempVertices;

    public DiamondScreen() {
        super();
        lastTouchPosition = new Vector2();
        skeletonBounds = new Rectangle();
        tempPosition = new Vector2();
        tempSize = new Vector2();
        tempVertices = new FloatArray();
    }

    @Override
    public void show() {
        viewport = new StretchViewport(800, 800);
        stage = new Stage(viewport);

        skeletonRenderer = new SkeletonRenderer();
        skeletonRenderer.setPremultipliedAlpha(true);

        shapeRenderer = new ShapeRenderer();

        initializeSkeleton();
        updateSkeletonBounds();

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
                    skeleton.setPosition(skeleton.getX() + deltaX, skeleton.getY() + deltaY);
                    lastTouchPosition.set(x, y);
                }
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                isDragging = false;
            }
        });

        addInputProcessor(stage);
    }

    private void initializeSkeleton() {
        String atlasPath = Constants.DiamondScreen.ATLAS;
        String skeletonPath = Constants.DiamondScreen.JSON;
        skeleton = game.getSpineAnimationHandler().createSkeleton(atlasPath, skeletonPath);
        state = game.getSpineAnimationHandler().createAnimationState(skeleton);
        state.setAnimation(0, "rotating-animation", true);
    }

    private void updateSkeletonBounds() {
        skeleton.getBounds(tempPosition, tempSize, tempVertices);
        skeletonBounds.set(tempPosition.x, tempPosition.y, tempSize.x, tempSize.y);
    }


    private boolean isOverSkeleton(float x, float y) {
        if (skeletonBounds.contains(x, y)) {
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

    public void update(float delta) {
        state.update(delta);
        state.apply(skeleton);
        skeleton.updateWorldTransform();
        updateSkeletonBounds();
        stage.act(delta);
        updateSkeletonBounds();
    }

    @Override
    public void render(float delta) {
        update(delta);
        clearScreen(0.5f, 0.5f, 0.5f, 1);

        viewport.apply();
        game.getBatch().setProjectionMatrix(viewport.getCamera().combined);
        game.getBatch().begin();
        skeletonRenderer.draw(game.getBatch(), skeleton);
        game.getBatch().end();

        // Draw debug shapes
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        // Draw circle at skeleton's root position
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.circle(skeleton.getX(), skeleton.getY(), 10);

        shapeRenderer.end();

        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        setSkeletonScale(skeleton, 0.8f, 0.8f, viewport);
        setSkeletonPosition(skeleton, viewport.getWorldWidth() / 2, 0);
        updateSkeletonBounds();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (stage != null) {
            stage.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}
