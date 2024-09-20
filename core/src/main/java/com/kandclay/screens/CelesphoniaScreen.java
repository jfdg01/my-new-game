package com.kandclay.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.*;
import com.esotericsoftware.spine.*;
import com.kandclay.utils.Constants;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class CelesphoniaScreen extends BaseScreen {
    private Stage stage;
    private Skeleton skeleton;
    private AnimationState state;
    private SkeletonRenderer skeletonRenderer;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;

    public CelesphoniaScreen() {
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
        updateSkeletonBounds();

        setupSkeletonDragging(stage);
        addInputProcessor(stage);

        setSkeletonScale(skeleton, viewport, 0.7f, 0.7f);
    }

    private void setBoneScale(Skeleton skeleton, String boneName, float scaleX, float scaleY) {
        Bone bone = skeleton.findBone(boneName);
        if (bone != null) {
            bone.setScaleX(scaleX);
            bone.setScaleY(scaleY);
        }
    }

    private void initializeSkeleton() {
        String atlasPath = Constants.Celesphonia.ATLAS;
        String skeletonPath = Constants.Celesphonia.JSON;
        skeleton = game.getSpineAnimationHandler().createSkeleton(atlasPath, skeletonPath);
        state = game.getSpineAnimationHandler().createAnimationState(skeleton);
        state.setAnimation(0, "Style_A_1_Fast", true);
        skeletons.add(skeleton);
    }

    public void update(float delta) {

        // update animation
        state.update(delta);
        skeleton.update(delta);

        // Apply the animation state
        state.apply(skeleton);
        skeleton.updateWorldTransform(Skeleton.Physics.update);
        updateSkeletonBounds();

        // Update the stage (for UI interactions)
        stage.act(delta);
    }

    @Override
    public void render(float delta) {
        update(delta);
        clearScreen(0.1f, 0.1f, 0.1f, 1);

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

