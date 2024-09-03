package com.kandclay.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.spine.*;
import com.kandclay.utils.Constants;

public class PenetrationTestScreen extends BaseScreen {
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

    private TextField attachmentNameField;
    private Button upButton;
    private TextButton downButton;

    private List<String> drawOrderList; // List to display the draw order
    private ScrollPane scrollPane;

    public PenetrationTestScreen() {
        super();
    }

    private void initializeUI() {
        Skin skin = game.getAssetManager().get(Constants.Skin.JSON, Skin.class);

        attachmentNameField = new TextField("", skin);
        attachmentNameField.setPosition(10, 10);
        attachmentNameField.setSize(200, 30);

        upButton = new TextButton("Up", skin);
        upButton.setPosition(220, 10);
        upButton.setSize(80, 30);
        upButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                modifyDrawOrder(skeleton, attachmentNameField.getText(), UP);
                updateDrawOrderList();
            }
        });

        downButton = new TextButton("Down", skin);
        downButton.setPosition(310, 10);
        downButton.setSize(80, 30);
        downButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                modifyDrawOrder(skeleton, attachmentNameField.getText(), DOWN);
                updateDrawOrderList();
            }
        });

        TextButton swapButton = new TextButton("Swap", skin);
        swapButton.setPosition(130, 10);
        swapButton.setSize(80, 30);
        swapButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Call the swap method for "anl-clipping" and "vgn-clipping"
                swapSlotsInDrawOrder(skeleton, "anl-clipping", "vgn-clipping");
                updateDrawOrderList();
            }
        });

        // Initialize the list and scroll pane for draw order
        drawOrderList = new List<String>(skin);
        scrollPane = new ScrollPane(drawOrderList, skin);

        // Adjust size and position for upper right corner
        scrollPane.setSize(150, 200); // Smaller size
        scrollPane.setPosition(640, 590); // Position in upper right corner

        // Optionally, use a Table for better layout management
        Table table = new Table();
        table.setFillParent(true);
        table.top().right(); // Align table to top-right
        table.add(scrollPane).size(150, 200).padTop(10).padRight(10); // Set the size and padding

        stage.addActor(table);
        stage.addActor(attachmentNameField);
        stage.addActor(upButton);
        stage.addActor(downButton);
        stage.addActor(swapButton);
    }

    @Override
    public void show() {
        viewport = new FitViewport(800, 800);
        stage = new Stage(viewport);

        skeletonRenderer = new SkeletonRenderer();
        skeletonRenderer.setPremultipliedAlpha(true);

        shapeRenderer = new ShapeRenderer();

        initializeUI();

        initializeSkeleton();

        updateDrawOrderList();

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
            if (distance < 20) { // In pixels
                Gdx.app.log("Testcreen", "Touching bone: " + targetBone.getData().getName());
                return true;
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

        updateDrawOrderList();

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

    private void updateDrawOrderList() {
        Array<String> slotNames = new Array<String>();
        Array<Slot> drawOrder = skeleton.getDrawOrder();
        for (int i = drawOrder.size - 1; i >= 0; i--) {
            slotNames.add(drawOrder.get(i).getData().getName());
        }
        drawOrderList.setItems(slotNames);
    }

    public void swapSlotsInDrawOrder(Skeleton skeleton, String slotName1, String slotName2) {
        Array<Slot> drawOrder = skeleton.getDrawOrder();
        int index1 = -1;
        int index2 = -1;

        // Find the indices of the two slots
        for (int i = 0; i < drawOrder.size; i++) {
            String currentSlotName = drawOrder.get(i).getData().getName();
            if (currentSlotName.equals(slotName1)) {
                index1 = i;
            } else if (currentSlotName.equals(slotName2)) {
                index2 = i;
            }

            // If both slots are found, no need to continue the loop
            if (index1 != -1 && index2 != -1) {
                break;
            }
        }

        // If both slots are found, swap them
        if (index1 != -1 && index2 != -1) {
            Slot tempSlot = drawOrder.get(index1);
            drawOrder.set(index1, drawOrder.get(index2));
            drawOrder.set(index2, tempSlot);

            // Update the skeleton's draw order
            skeleton.setDrawOrder(drawOrder);

            // Update the UI list
            updateDrawOrderList();
        } else {
            // Handle the case where one or both slots were not found
            if (index1 == -1) {
                Gdx.app.log("SwapSlots", "Slot not found: " + slotName1);
            }
            if (index2 == -1) {
                Gdx.app.log("SwapSlots", "Slot not found: " + slotName2);
            }
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
