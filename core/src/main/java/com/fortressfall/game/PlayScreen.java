package com.fortressfall.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class PlayScreen extends ScreenAdapter {
    private static final float GROUND_Y = 82f;
    private static final float BUILDING_Y = GROUND_Y + 8f;
    private static final Color GRASS = new Color(0.19f, 0.28f, 0.2f, 1f);
    private static final Color DIRT = new Color(0.48f, 0.36f, 0.22f, 1f);
    private static final Color DIRT_SHADOW = new Color(0.31f, 0.23f, 0.16f, 1f);
    private static final Color HUD_BACKGROUND = new Color(0.06f, 0.1f, 0.15f, 1f);
    private static final Color CASTLE = new Color(0.46f, 0.48f, 0.55f, 1f);
    private static final Color CASTLE_DARK = new Color(0.14f, 0.17f, 0.22f, 1f);
    private static final Color DOOR = new Color(0.22f, 0.16f, 0.14f, 1f);
    private static final Color PLOT = new Color(0.66f, 0.55f, 0.36f, 1f);
    private static final Color EMPTY_PLOT = new Color(0.28f, 0.4f, 0.27f, 1f);
    private static final Color MINE = new Color(0.48f, 0.36f, 0.19f, 1f);
    private static final Color TOWER = new Color(0.44f, 0.46f, 0.49f, 1f);
    private static final Color WALL = new Color(0.5f, 0.52f, 0.55f, 1f);
    private static final Color KNIGHT = new Color(0.58f, 0.67f, 0.8f, 1f);
    private static final Color ARCHER = new Color(0.34f, 0.56f, 0.35f, 1f);
    private static final Color ENEMY = new Color(0.65f, 0.19f, 0.17f, 1f);
    private static final Color BAR_BACKGROUND = new Color(0.18f, 0.12f, 0.12f, 1f);
    private static final Color HEALTH = new Color(0.29f, 0.78f, 0.31f, 1f);

    private final Main game;
    private final GameWorld world = new GameWorld();

    private SpriteBatch batch;
    private Texture pixel;
    private BitmapFont font;
    private GameWorld.BuildingType selectedBuilding = GameWorld.BuildingType.MINE;
    private String message = "";
    private float messageTimer;

    public PlayScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixel = new Texture(pixmap);
        pixmap.dispose();
    }

    @Override
    public void render(float delta) {
        handleInput();
        world.update(delta, Gdx.graphics.getWidth());
        if (messageTimer > 0f) {
            messageTimer -= delta;
        }

        Gdx.gl.glClearColor(0.09f, 0.14f, 0.19f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        drawBackground();
        drawCastle();
        drawPlots();
        drawEntities();
        drawHud();
        batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuScreen(game));
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
            recruit(GameWorld.UnitType.KNIGHT);
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) {
            recruit(GameWorld.UnitType.ARCHER);
            return;
        }
        if (world.isWaveActive()) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            selectBuilding(GameWorld.BuildingType.MINE);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            selectBuilding(GameWorld.BuildingType.TOWER);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            selectBuilding(GameWorld.BuildingType.WALL);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (world.startWave()) {
                setMessage("Wave " + world.getWave() + " incoming!");
            }
        } else if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            placeBuilding();
        }
    }

    private void selectBuilding(GameWorld.BuildingType type) {
        selectedBuilding = type;
        setMessage(type.label + " selected: " + type.cost + " gold. Click a plot.");
    }

    private void recruit(GameWorld.UnitType type) {
        setMessage(world.train(type) ? type.label + " recruited."
                : "Need " + type.cost + " gold to recruit a " + type.label + ".");
    }

    private void placeBuilding() {
        float width = Gdx.graphics.getWidth();
        float clickX = Gdx.input.getX();
        float clickY = Gdx.graphics.getHeight() - Gdx.input.getY();
        if (clickY < GROUND_Y || clickY > Gdx.graphics.getHeight() - 115f) {
            return;
        }

        int nearestSlot = -1;
        float nearestDistance = 55f;
        for (int slot = 0; slot < GameWorld.SLOT_COUNT; slot++) {
            float distance = Math.abs(clickX - GameWorld.slotX(slot, width));
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestSlot = slot;
            }
        }

        if (nearestSlot < 0) {
            setMessage("Click closer to an empty plot.");
        } else if (world.getBuildingAt(nearestSlot) != null) {
            GameWorld.Building building = world.getBuildingAt(nearestSlot);
            setMessage(building.type.label + " is already built here.");
        } else if (world.build(selectedBuilding, nearestSlot)) {
            setMessage(selectedBuilding.label + " built.");
        } else {
            setMessage("Plot occupied or not enough gold.");
        }
    }

    private void drawBackground() {
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        drawRect(0, 0, width, GROUND_Y, GRASS);
        drawRect(0, GROUND_Y, width, 5, DIRT);
        drawRect(0, GROUND_Y + 5, width, 3, DIRT_SHADOW);
        drawRect(0, height - 170, width, 170, HUD_BACKGROUND);
    }

    private void drawCastle() {
        drawRect(34, GROUND_Y + 8, 76, 75, CASTLE);
        drawRect(28, GROUND_Y + 78, 88, 15, CASTLE);
        drawRect(44, GROUND_Y + 44, 16, 20, CASTLE_DARK);
        drawRect(82, GROUND_Y + 8, 15, 28, DOOR);
        font.draw(batch, "CASTLE", 44, GROUND_Y + 106);
        drawHealthBar(34, GROUND_Y + 96, 76, world.getCastleHealth() / 200f);
    }

    private void drawPlots() {
        for (int slot = 0; slot < GameWorld.SLOT_COUNT; slot++) {
            float x = GameWorld.slotX(slot, Gdx.graphics.getWidth());
            drawRect(x - 25, GROUND_Y + 2, 50, 5, PLOT);
            GameWorld.Building building = world.getBuildingAt(slot);
            if (building == null) {
                drawRect(x - 20, GROUND_Y + 7, 40, 3, EMPTY_PLOT);
                continue;
            }

            switch (building.type) {
                case MINE:
                    drawRect(x - 18, BUILDING_Y, 36, 36, MINE);
                    font.draw(batch, "MINE", x - 16, BUILDING_Y + 22);
                    break;
                case TOWER:
                    drawRect(x - 15, BUILDING_Y, 30, 70, TOWER);
                    font.draw(batch, "TOWER", x - 21, BUILDING_Y + 82);
                    break;
                case WALL:
                    drawRect(x - 22, BUILDING_Y, 44, 44, WALL);
                    font.draw(batch, "WALL", x - 16, BUILDING_Y + 58);
                    break;
            }
            if (building.type == GameWorld.BuildingType.WALL) {
                drawHealthBar(x - 22, BUILDING_Y + 49, 44, building.health / building.maxHealth);
            }
        }
    }

    private void drawEntities() {
        for (GameWorld.Unit unit : world.getUnits()) {
            Color color = unit.type == GameWorld.UnitType.KNIGHT ? KNIGHT : ARCHER;
            drawRect(unit.x - 10, GROUND_Y + 8, 20, 42, color);
            drawHealthBar(unit.x - 11, GROUND_Y + 53, 22, unit.health / unit.maxHealth);
        }

        for (GameWorld.Enemy enemy : world.getEnemies()) {
            drawRect(enemy.x - 15, GROUND_Y + 8, 30, 48, ENEMY);
            drawHealthBar(enemy.x - 15, GROUND_Y + 61, 30, enemy.health / enemy.maxHealth);
        }
    }

    private void drawHud() {
        float height = Gdx.graphics.getHeight();
        font.draw(batch, "FortressFall   Gold: " + world.getGold()
                + "   Castle: " + world.getCastleHealth()
                + "   Wave: " + world.getWave(), 14, height - 20);
        if (world.isWaveActive()) {
            font.draw(batch, "WAVE " + world.getWave() + "   ENEMIES: "
                    + world.getEnemiesRemaining(), 14, height - 44);
        }

        if (messageTimer > 0f) {
            font.draw(batch, message, 14, height - 67);
        }
    }

    private void drawHealthBar(float x, float y, float width, float health) {
        float clampedHealth = Math.max(0f, Math.min(1f, health));
        drawRect(x, y, width, 5, BAR_BACKGROUND);
        drawRect(x, y, width * clampedHealth, 5, HEALTH);
    }

    private void drawRect(float x, float y, float width, float height, Color color) {
        batch.setColor(color);
        batch.draw(pixel, x, y, width, height);
        batch.setColor(Color.WHITE);
    }

    private void setMessage(String text) {
        message = text;
        messageTimer = 3f;
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        if (pixel != null) pixel.dispose();
        if (font != null) font.dispose();
        if (batch != null) batch.dispose();
        pixel = null;
        font = null;
        batch = null;
    }
}
