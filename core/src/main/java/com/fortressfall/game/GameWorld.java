package com.fortressfall.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class GameWorld {
    public static final int SLOT_COUNT = 5;

    public enum BuildingType {
        MINE("Mine", 55, 90),
        TOWER("Arrow tower", 75, 90),
        WALL("Wall", 40, 100);

        public final String label;
        public final int cost, health;

        BuildingType(String label, int cost, int health) {
            this.label = label; 
            this.cost = cost; 
            this.health = health;
        }
    }

    public enum UnitType {
        KNIGHT("knight", 50, 70, 52f, 42f, 17f, 0.8f),
        ARCHER("archer", 65, 42, 35f, 190f, 10f, 1.1f);

        public final String label;
        public final int cost;
        public final float health, speed, attackRange, damage, attackInterval;

        UnitType(String label, int cost, float health, float speed, float attackRange, float damage, float interval) {
            this.label = label;
            this.cost = cost; 
            this.health = health; 
            this.speed = speed; 
            this.attackRange = attackRange;
            this.damage = damage; 
            this.attackInterval = interval;
        }
    }

    public static class Building {
        public final BuildingType type;
        public final int slot;
        public float maxHealth;
        public float health;
        private float attackTimer;

        private Building(BuildingType type, int slot, float health) {
            this.type = type;
            this.slot = slot;
            this.maxHealth = health;
            this.health = health;
        }
    }

    public static class Unit {
        public final UnitType type;
        public final float maxHealth;
        public float x;
        public float health;
        private float attackTimer;

        private Unit(UnitType type) {
            this.type = type;
            this.maxHealth = type.health;
            this.health = maxHealth;
        }
    }

    public static class Enemy {
        public final int wave;
        public final float maxHealth;
        public final float speed;
        public final int damage, reward;
        public float x;
        public float health;
        private float attackTimer;

        private Enemy(int wave, float x) {
            this.wave = wave;
            this.x = x;
            maxHealth = 38f + wave * 8f;
            speed = 24f + wave * 1.4f;
            damage = 8 + wave * 2;
            reward = 10 + wave * 2;
            this.health = maxHealth;
        }
    }

    private final List<Building> buildings = new ArrayList<Building>();
    private final List<Unit> units = new ArrayList<Unit>();
    private final List<Enemy> enemies = new ArrayList<Enemy>();

    private int gold = 180;
    private int wave;
    private int enemiesLeftToSpawn;
    private int castleHealth = 200;
    private float productionTimer;
    private float spawnTimer;
    private boolean waveActive;

    public void update(float delta, float worldWidth) {
        float step = Math.min(delta, 0.1f);
        updateProduction(step);
        updateSpawning(step, worldWidth);
        updateCombat(step, worldWidth);
        finishWaveIfClear();
    }

    public boolean build(BuildingType type, int slot) {
        if (waveActive || slot < 0 || slot >= SLOT_COUNT || getBuildingAt(slot) != null) {
            return false;
        }

        if (gold < type.cost) {
            return false;
        }

        gold -= type.cost;
        buildings.add(new Building(type, slot, type.health));
        return true;
    }

    public boolean train(UnitType type) {
        if (gold < type.cost) {
            return false;
        }

        gold -= type.cost;
        Unit unit = new Unit(type);
        unit.x = 105f;
        units.add(unit);
        return true;
    }

    public boolean startWave() {
        if (waveActive) {
            return false;
        }

        wave++;
        enemiesLeftToSpawn = 3 + wave * 2;
        spawnTimer = 0f;
        waveActive = true;
        return true;
    }

    public static int buildingCost(BuildingType type) {
        return type.cost;
    }

    public static float slotX(int slot, float worldWidth) {
        float left = worldWidth * 0.25f;
        float right = worldWidth * 0.84f;
        return left + (right - left) * slot / (SLOT_COUNT - 1);
    }

    public Building getBuildingAt(int slot) {
        for (Building building : buildings) {
            if (building.slot == slot) {
                return building;
            }
        }
        return null;
    }

    public List<Building> getBuildings() { return Collections.unmodifiableList(buildings); }
    public List<Unit> getUnits() { return Collections.unmodifiableList(units); }
    public List<Enemy> getEnemies() { return Collections.unmodifiableList(enemies); }
    public int getGold() { return gold; }
    public int getWave() { return wave; }
    public int getEnemiesRemaining() { return enemiesLeftToSpawn + enemies.size(); }
    public int getCastleHealth() { return castleHealth; }
    public boolean isWaveActive() { return waveActive; }

    private void updateProduction(float delta) {
        productionTimer += delta;
        while (productionTimer >= 2.5f) {
            productionTimer -= 2.5f;
            int mineProduction = 0;
            for (Building building : buildings) {
                if (building.type == BuildingType.MINE) {
                    mineProduction += 6;
                }
            }
            gold += mineProduction;
        }
    }

    private void updateSpawning(float delta, float worldWidth) {
        if (!waveActive || enemiesLeftToSpawn == 0) {
            return;
        }

        spawnTimer -= delta;
        if (spawnTimer <= 0f) {
            enemies.add(new Enemy(wave, worldWidth - 45f));
            enemiesLeftToSpawn--;
            spawnTimer = Math.max(0.55f, 1.7f - wave * 0.07f);
        }
    }

    private void updateCombat(float delta, float worldWidth) {
        updateTowers(delta, worldWidth);
        updateUnits(delta, worldWidth);
        removeDefeatedEnemies();
        updateEnemies(delta, worldWidth);
        removeDestroyedBuildings();
        removeDefeatedEnemies();
        removeDefeatedUnits();
    }

    private void updateTowers(float delta, float worldWidth) {
        for (Building building : buildings) {
            if (building.type != BuildingType.TOWER) {
                continue;
            }

            building.attackTimer -= delta;
            if (building.attackTimer > 0f) {
                continue;
            }

            float towerX = slotX(building.slot, worldWidth);
            Enemy target = findNearestEnemy(towerX, 300f);
            if (target != null) {
                target.health -= 15f;
                building.attackTimer = 0.75f;
            }
        }
    }

    private void updateUnits(float delta, float worldWidth) {
        for (Unit unit : units) {
            unit.attackTimer -= delta;
            Enemy target = findNearestEnemy(unit.x, unit.type.attackRange);
            if (target != null) {
                if (unit.attackTimer <= 0f) {
                    target.health -= unit.type.damage;
                    unit.attackTimer = unit.type.attackInterval;
                }
            } else {
                unit.x = Math.min(unit.x + unit.type.speed * delta,
                        Math.max(105f, worldWidth - 40f));
            }
        }
    }

    private void updateEnemies(float delta, float worldWidth) {
        for (Enemy enemy : enemies) {
            Unit target = findNearestUnit(enemy.x, 28f);
            if (target != null) {
                enemy.attackTimer -= delta;
                if (enemy.attackTimer <= 0f) {
                    target.health -= getEnemyDamage(enemy);
                    enemy.attackTimer = 0.9f;
                }
                continue;
            }

            Building blockingWall = findBlockingWall(enemy.x, worldWidth);
            if (blockingWall != null) {
                enemy.attackTimer -= delta;
                if (enemy.attackTimer <= 0f) {
                    blockingWall.health -= getEnemyDamage(enemy);
                    enemy.attackTimer = 0.9f;
                }
                continue;
            }

            if (enemy.x <= 88f) {
                enemy.attackTimer -= delta;
                if (enemy.attackTimer <= 0f) {
                    castleHealth = Math.max(0, castleHealth - getEnemyDamage(enemy));
                    enemy.attackTimer = 1f;
                }
            } else {
                enemy.x -= enemy.speed * delta;
            }
        }
    }

    private Unit findNearestUnit(float x, float range) {
        Unit nearest = null;
        float nearestDistance = range;
        for (Unit unit : units) {
            if (unit.health <= 0f) {
                continue;
            }
            float distance = Math.abs(unit.x - x);
            if (distance <= nearestDistance) {
                nearest = unit;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private int getEnemyDamage(Enemy enemy) {
        return enemy.damage;
    }

    private Building findBlockingWall(float enemyX, float worldWidth) {
        for (Building building : buildings) {
            if (building.type == BuildingType.WALL) {
                float wallX = slotX(building.slot, worldWidth);
                if (enemyX <= wallX + 24f && enemyX >= wallX - 24f) {
                    return building;
                }
            }
        }
        return null;
    }

    private Enemy findNearestEnemy(float x, float range) {
        Enemy nearest = null;
        float nearestDistance = range;
        for (Enemy enemy : enemies) {
            if (enemy.health <= 0f) {
                continue;
            }
            float distance = Math.abs(enemy.x - x);
            if (distance <= nearestDistance) {
                nearest = enemy;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private void removeDefeatedEnemies() {
        Iterator<Enemy> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();
            if (enemy.health <= 0f) {
                gold += enemy.reward;
                iterator.remove();
            }
        }
    }

    private void removeDefeatedUnits() {
        units.removeIf(unit -> unit.health <= 0f);
    }

    private void removeDestroyedBuildings() {
        Iterator<Building> iterator = buildings.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().health <= 0f) {
                iterator.remove();
            }
        }
    }

    private void finishWaveIfClear() {
        if (waveActive && enemiesLeftToSpawn == 0 && enemies.isEmpty()) {
            waveActive = false;
            gold += wave * 15;
        }
    }
}
