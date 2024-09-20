package com.kandclay.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.kandclay.handlers.SpineAnimationHandler;

import java.util.Iterator;

public class TrailDot {
    public Skeleton skeleton;
    public AnimationState state;
    public SkeletonRenderer renderer;
    public static SpineAnimationHandler spineAnimationHandler;
    private static final SnapshotArray<TrailDot> trailDots = new SnapshotArray<TrailDot>();
    private static int trailDotCount = 0;
    public float x, y;
    public Viewport viewport;

    private TrailDot(Skeleton skeleton, AnimationState state, SkeletonRenderer renderer, float x, float y, Viewport viewport) {
        this.skeleton = skeleton;
        this.state = state;
        this.renderer = renderer;
        this.x = x;
        this.y = y;
        this.viewport = viewport;
    }

    public static void setSpineAnimationHandler(SpineAnimationHandler handler) {
        spineAnimationHandler = handler;
    }

    public static void createTrailDot(float x, float y, Viewport viewport) {
        Rectangle screenBounds = new Rectangle(
            viewport.getScreenX(),
            viewport.getScreenY(),
            viewport.getScreenWidth(),
            viewport.getScreenHeight()
        );

        int screenY = Gdx.graphics.getHeight() - Gdx.input.getY(); // Flip Y-coordinate

        if (!screenBounds.contains(Gdx.input.getX(), screenY)) {
            return;
        }

        float hue = (trailDotCount % Constants.TrailDot.NUMBER_OF_COLORS);

        Color currentColor = new Color();
        currentColor.fromHsv(hue, Constants.TrailDot.SATURATION, Constants.TrailDot.VALUE);
        currentColor.a = Constants.TrailDot.ALPHA;

        String trailAtlasPath = Constants.TrailDot.ATLAS;
        String trailSkeletonPath = Constants.TrailDot.JSON;

        Skeleton trailSkeleton = spineAnimationHandler.createSkeleton(trailAtlasPath, trailSkeletonPath);
        AnimationState trailState = spineAnimationHandler.createAnimationState(trailSkeleton);
        SkeletonRenderer trailRenderer = new SkeletonRenderer();
        trailRenderer.setPremultipliedAlpha(true);

        float randomScale = MathUtils.random(Constants.TrailDot.MIN_SCALE, Constants.TrailDot.MAX_SCALE);
        float randomRotation = MathUtils.random(Constants.TrailDot.MIN_ROTATION, Constants.TrailDot.MAX_ROTATION);

        trailSkeleton.setPosition(x, y);
        trailSkeleton.setColor(currentColor);
        trailSkeleton.setScale(randomScale, randomScale);
        trailSkeleton.getRootBone().setRotation(randomRotation);

        trailState.setAnimation(0, "animation", false);

        trailDots.add(new TrailDot(trailSkeleton, trailState, trailRenderer, x, y, viewport));
        trailDotCount++;
    }

    public static void renderTrail(Batch batch, Viewport viewport) {
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        Iterator<TrailDot> iterator = trailDots.iterator();
        while (iterator.hasNext()) {
            TrailDot trailDot = iterator.next();
            if (trailDot.viewport == viewport) {
                trailDot.state.update(Gdx.graphics.getDeltaTime());
                trailDot.state.apply(trailDot.skeleton);
                trailDot.skeleton.updateWorldTransform(Skeleton.Physics.update);
                trailDot.skeleton.setPosition(trailDot.x, trailDot.y);

                trailDot.renderer.draw(batch, trailDot.skeleton);

                if (trailDot.state.getCurrent(0) == null || trailDot.state.getCurrent(0).isComplete()) {
                    iterator.remove();
                }
            }
        }
        batch.end();
    }
}
