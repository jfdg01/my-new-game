package com.kandclay.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonRenderer;
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

    private void initializeSkeleton() {
        String atlasPath = Constants.TestScreen.ATLAS;
        String skeletonPath = Constants.TestScreen.JSON;

        skeleton = game.getSpineAnimationHandler().createSkeleton(atlasPath, skeletonPath);
        state = game.getSpineAnimationHandler().createAnimationState(skeleton);

        setSkeletonScale(skeleton, 0.8f, 0.8f, viewport);

        // state.setAnimation(0, "animation", true);

        targetBone = skeleton.findBone("target2");
    }

    private void moveBone(float deltaX, float deltaY) {
        if (targetBone != null) {
            targetBone.setX(targetBone.getX() + deltaX);
            targetBone.setY(targetBone.getY() + deltaY);
        }
    }

    @Override
    public void render(float delta) {
        clearScreen(255, 255, 255, 0);

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
//        setSkeletonScale(skeleton, 0.8f, 0.8f, viewport);
        setSkeletonPosition(skeleton, 0,0);
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
