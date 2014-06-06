package com.frozendogtears.cavegame;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.frozendogtears.common.Assets;

public class JetPack {
	private ParticleEffect jetpackEffect;
	private Body body;
	private float maxFuelTime; // seconds
	private float jetPackRestTime; // seconds
	// public float NO_BATTERY_FLICKER_TIME = 1; // seconds
	private float rechargeMultiplier;
	private float activeTime;
	private float restTimer;
	private boolean hasFuel;

	public boolean hasFuel() {
		return hasFuel;
	}

	public float getFuelTime() {
		return maxFuelTime;
	}

	public float getRestTimer() {
		return restTimer;
	}
	
	public boolean isActive(){
		return activeTime > 0;
	}
	
	public JetPack(World world, Body body) {
		jetpackEffect = Assets.jetpackEffect;
		maxFuelTime = 40;
		jetPackRestTime = 3;
		rechargeMultiplier = 1.8f;
		activeTime = 0;
		restTimer = jetPackRestTime;
		hasFuel = true;

		attachToBody(body);
		jetpackEffect.start();
	}

	public void update(float deltaTime) {
		if (body != null) {
			final Vector2 pos = body.getPosition();
			jetpackEffect.setPosition(pos.x, pos.y);
		}

		jetpackEffect.update(deltaTime);
	}

	public void update(float deltaTime, Vector2 pos) {
		jetpackEffect.setPosition(body.getWorldCenter().x,
				body.getWorldCenter().y);
		jetpackEffect.update(deltaTime);
	}

	public void attachToBody(Body body) {
		this.body = body;
	}

	public void draw(SpriteBatch batch) {
		jetpackEffect.draw(batch);
	}

	public void reset() {
		jetpackEffect.reset();
	}

	public void dispose() {
		jetpackEffect.dispose();
	}
}
