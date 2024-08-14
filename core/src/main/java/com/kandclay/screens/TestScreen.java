package com.kandclay.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.spine.*;
import com.kandclay.utils.Constants;

public class TestScreen extends BaseScreen {
    private Stage stage;
    private Skeleton skeleton;
    private AnimationState state;
    private SkeletonRenderer skeletonRenderer;
    private Viewport viewport;
    private Bone targetBone;
    private Vector2 lastMousePosition = new Vector2();
    private boolean isDragging = false;
    private ShapeRenderer shapeRenderer;

    private static final boolean UP = true;
    private static final boolean DOWN = false;

    public TestScreen() {
        super();
    }

    @Override
    public void show() {
        viewport = new FitViewport(800, 800);
        stage = new Stage(viewport);

        skeletonRenderer = new SkeletonRenderer();
        skeletonRenderer.setPremultipliedAlpha(true);

        shapeRenderer = new ShapeRenderer();

        initializeSkeleton();

        stage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Vector2 worldCoords = new Vector2(x, y);
                if (isTouchingBone(worldCoords)) {
                    isDragging = true;
                    lastMousePosition.set(worldCoords);
                }
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                isDragging = false;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if (isDragging) {
                    Vector2 worldCoords = new Vector2(x, y);
                    float deltaX = worldCoords.x - lastMousePosition.x;
                    float deltaY = worldCoords.y - lastMousePosition.y;
                    lastMousePosition.set(worldCoords);
                    moveBone(deltaX, deltaY);
                }
            }
        });

        Gdx.input.setInputProcessor(stage);
    }

    private boolean isTouchingBone(Vector2 touchPoint) {
        if (targetBone != null) {
            float boneX = targetBone.getWorldX();
            float boneY = targetBone.getWorldY();
            float distance = Vector2.dst(touchPoint.x, touchPoint.y, boneX, boneY);
//            Gdx.app.log("", "\nBone (" + floor(boneX) + "," + floor(boneY) +
//                ")\nTouchPoint (" + floor(touchPoint.x) + "," + floor(touchPoint.y) + ") \nDistance: " + floor(distance));
            if (distance < 20) {
                Gdx.app.log("ZeldaScreen", "Touching bone: " + targetBone.getData().getName());
                return true; // Adjust this value to change the touch area size
            }
        }
        return false;
    }

    private void showDrawOrder(Skeleton skeleton) {
        for (int i = skeleton.getDrawOrder().size - 1; i >= 0; i--) {
            Slot slot = skeleton.getDrawOrder().get(i);
            Gdx.app.log("DrawOrder", slot.getData().getName());
        }
        Gdx.app.log("DrawOrder", "-----------------");
    }

    private void initializeSkeleton() {
        String atlasPath = Constants.TestScreen.ATLAS;
        String skeletonPath = Constants.TestScreen.JSON;

        skeleton = game.getSpineAnimationHandler().createSkeleton(atlasPath, skeletonPath);
        state = game.getSpineAnimationHandler().createAnimationState(skeleton);

        showDrawOrder(skeleton);

        modifyDrawOrder(skeleton, "anl-clipping", DOWN);

        showDrawOrder(skeleton);

//        Gdx.app.log("DrawOrder", "-----------------");

        // state.setAnimation(0, "animation", true);

        targetBone = skeleton.findBone("bone");
    }

    public void modifyDrawOrder(Skeleton skeleton, String slotName, boolean moveUp) {
        modifyDrawOrder(skeleton, slotName, moveUp, 1);
    }

    public void modifyDrawOrder(Skeleton skeleton, String slotName, boolean moveUp, int times) {
        Array<Slot> drawOrder = skeleton.getDrawOrder();
        int currentIndex = -1;

        // Find the index of the specified slot
        for (int i = 0; i < drawOrder.size; i++) {
            if (drawOrder.get(i).getData().getName().equals(slotName)) {
                currentIndex = i;
                break;
            }
        }

        // If the slot is found, modify its position in the draw order
        if (currentIndex != -1) {
            Slot slotToMove = drawOrder.get(currentIndex);
            int newIndex;

            if (moveUp) {
                // Move up (towards the end of the array)
                newIndex = Math.min(currentIndex + times, drawOrder.size - 1);
            } else {
                // Move down (towards the beginning of the array)
                newIndex = Math.max(currentIndex - times, 0);
            }

            // Perform the move
            if (newIndex != currentIndex) {
                drawOrder.removeIndex(currentIndex);
                drawOrder.insert(newIndex, slotToMove);

                // Update the skeleton's draw order
                skeleton.setDrawOrder(drawOrder);
            }
        } else {
            System.out.println("Slot not found: " + slotName);
        }
    }

    private void moveBone(float deltaX, float deltaY) {
        if (targetBone != null) {
            targetBone.setX(targetBone.getX() + deltaX);
            targetBone.setY(targetBone.getY() + deltaY);
        }
    }

    @Override
    public void render(float delta) {
        clearScreen(255, 100, 100, 0);

        state.update(delta);
        state.apply(skeleton);
        skeleton.updateWorldTransform();

        viewport.apply();
        game.getBatch().setProjectionMatrix(viewport.getCamera().combined);
        game.getBatch().begin();
        skeletonRenderer.draw(game.getBatch(), skeleton);
        game.getBatch().end();

        // Draw debug circles
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw red circle for mouse position
        shapeRenderer.setColor(Color.CORAL);
        Vector2 mouseWorldPos = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        shapeRenderer.circle(mouseWorldPos.x, mouseWorldPos.y, 10);

        // Draw blue circle for target bone position
        if (targetBone != null) {
            shapeRenderer.setColor(Color.MAROON);
            shapeRenderer.circle(targetBone.getWorldX(), targetBone.getWorldY(), 10);
        }

        shapeRenderer.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        setSkeletonScale(skeleton, 0.8f, 0.8f, viewport);
        setSkeletonPosition(skeleton, 0,200);
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
