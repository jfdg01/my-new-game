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

public class DiamondScreen extends BaseScreen {
    private Stage stage;
    private Skeleton skeleton;
    private AnimationState state;
    private SkeletonRenderer skeletonRenderer;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;

    private Slider boneScaleSlider;
    private Slider boneHeightSlider;
    private TextButton toggleDirectionButton;
    private boolean isAnimationReversed = false;
    private float animationDuration;

    public DiamondScreen() {
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

        setBoneScale(skeleton, "middle-scale", 1f, 1 / 3f);

        setupSkeletonDragging(stage);
        setupUIElements();
        addInputProcessor(stage);

        setSkeletonScale(skeleton, viewport, 0.7f, 0.7f);
    }

    private void setupUIElements() {
        Skin skin = assetManager.get(Constants.Skin.JSON, Skin.class);

        // Create sliders
        boneScaleSlider = new Slider(0f, 1f, 0.05f, false, skin);
        boneScaleSlider.setValue(1f);
        boneScaleSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float scale = boneScaleSlider.getValue();
                setBoneScale(skeleton, "top-scale", scale, scale / 3f);
            }
        });

        boneHeightSlider = new Slider(-60f, 250f, 1f, false, skin);
        boneHeightSlider.setValue(0f);
        boneHeightSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float yTranslation = boneHeightSlider.getValue();
                setBoneYTranslation(skeleton, "lower-point", yTranslation);
            }
        });

        // Create button to toggle animation direction
        toggleDirectionButton = new TextButton("Reverse Animation", skin);
        toggleDirectionButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                toggleAnimationDirection();
            }
        });

        // Create a table to hold the sliders and button
        Table table = new Table();
        table.setFillParent(true);  // Make the table fill the screen
        table.top();  // Position table at the bottom of the screen

        // Add sliders and button to the table
        table.add(boneScaleSlider).width(200).pad(10);  // Add some padding
        table.row();  // Move to next row
        table.add(boneHeightSlider).width(200).pad(10);
        table.row();
        table.add(toggleDirectionButton).width(200).pad(10);

        // Add the table to the stage
        stage.addActor(table);
    }

    private void setBoneYTranslation(Skeleton skeleton, String boneName, float yTranslation) {
        Bone bone = skeleton.findBone(boneName);
        if (bone != null) {
            bone.setY(yTranslation);  // Set the Y position (height) of the bone
        }
    }

    private void toggleAnimationDirection() {
        isAnimationReversed = !isAnimationReversed;
        toggleDirectionButton.setText(isAnimationReversed ? "Forward Animation" : "Reverse Animation");

        if (isAnimationReversed) {
            // When reversing, set the time to the end of the animation to loop properly
            AnimationState.TrackEntry current = state.getCurrent(0);
            animationDuration = current.getAnimation().getDuration();
            current.setTrackTime(animationDuration - current.getTrackTime());  // Reverse from the current point
            current.setLoop(false);  // Disable native looping, as we'll handle it manually
        } else {
            // When going forward again, reset loop and continue normally
            state.getCurrent(0).setLoop(true);
        }
    }

    private void setBoneScale(Skeleton skeleton, String boneName, float scaleX, float scaleY) {
        Bone bone = skeleton.findBone(boneName);
        if (bone != null) {
            bone.setScaleX(scaleX);
            bone.setScaleY(scaleY);
        }
    }

    private void initializeSkeleton() {
        String atlasPath = Constants.DiamondScreen.ATLAS;
        String skeletonPath = Constants.DiamondScreen.JSON;
        skeleton = game.getSpineAnimationHandler().createSkeleton(atlasPath, skeletonPath);
        state = game.getSpineAnimationHandler().createAnimationState(skeleton);
        state.setAnimation(0, "rotating-animation", true);
        skeletons.add(skeleton);
    }

    public void update(float delta) {
        // Handle the animation update manually when it's reversed
        if (isAnimationReversed) {
            AnimationState.TrackEntry current = state.getCurrent(0);
            float newTrackTime = current.getTrackTime() - delta;

            // Check if we've reached the start of the animation, and if so, loop it back to the end
            if (newTrackTime <= 0) {
                current.setTrackTime(animationDuration);  // Loop back to the end of the animation
            } else {
                current.setTrackTime(newTrackTime);  // Continue reversing
            }
        } else {
            // Normal forward update
            state.update(delta);
        }

        // Apply the animation state
        state.apply(skeleton);
        skeleton.updateWorldTransform();
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
