package com.frozendogtears.cavegame;

import java.util.ArrayList;

import net.dermetfan.utils.libgdx.box2d.Box2DUtils;
import net.dermetfan.utils.libgdx.graphics.AnimatedBox2DSprite;
import net.dermetfan.utils.libgdx.graphics.AnimatedSprite;
import net.dermetfan.utils.libgdx.graphics.Box2DSprite;
import box2dLight.Light;
import box2dLight.PointLight;
import box2dLight.RayHandler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.frozendogtears.common.Assets;
import com.frozendogtears.common.GameConstants;
import com.frozendogtears.common.Globals;
import com.frozendogtears.common.Globals.FixtureUserData;
import com.frozendogtears.kryonet.NetworkClient;
import com.frozendogtears.kryonet.PlayerKeyDownUpdateRequest;
import com.frozendogtears.kryonet.PlayerKeyUpUpdateRequest;
import com.frozendogtears.kryonet.PlayerMouseUpdateRequest;
import com.frozendogtears.kryonet.PlayerPositionUpdateRequest;

public class Player extends InputAdapter {
	Body body;
	BodyDef bodyDef;
	Fixture fixture;
	float angle;
	float w, h;
	PolygonShape shape;
	Animation swimAnimation;
	Animation runAnimation;
	Animation idleAnimation;
	Animation jumpAnimation;
	AnimatedSprite animatedSprite;
	AnimatedBox2DSprite animatedBox2dSprite;
	Box2DSprite box2dSprite;
	
	private enum LookDirection {
		LEFT, RIGHT, UP, DOWN,
	}

	private LookDirection lookDirection;

	private Vector2 movement;
	private float speed;
	private float rotationSpeed;
	private Vector2 pos, oldPos, deltaPos;
	private float newAngle = 0;

	private boolean flashlightEnabled;
	private boolean isUnderWater;
	private boolean canUseJetpack;
	private boolean isUsingJetpack;
	private boolean switchAnimation;
	
	private PointLight playerlight;
	// private ConeLight flashlight;
	private FlashLight flashLight;
	private float lookAngle = 0;

	private JetPack jetPack;

	// DEBUG STUFF
	public static ArrayList<String> debugStrings = new ArrayList<String>();

	private static float SCALE = 2; // needed to make the "pixels" to scale

	public Player(World world, RayHandler rayHandler, Vector2 startPos) {
		angle = 0;
		flashlightEnabled = false;
		isUnderWater = false;
		canUseJetpack = false;
		isUsingJetpack = false;
		switchAnimation = false;
		lookDirection = LookDirection.RIGHT;
		speed = 5.5f;
		rotationSpeed = 3f;
		movement = Vector2.Zero;
		oldPos = pos;

		deltaPos = Vector2.Zero;

		bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(startPos);
		body = world.createBody(bodyDef);

//		swimAnimation = new Animation(1 / 3f, Assets.playerRunSprites);
//		swimAnimation.setPlayMode(Animation.PlayMode.LOOP);
		
		runAnimation = new Animation(1 / 3f, Assets.playerRunTextures);
		runAnimation.setPlayMode(Animation.PlayMode.LOOP);
		
		jumpAnimation = new Animation(1 / 3f, Assets.playerjumpingTextures);
		jumpAnimation.setPlayMode(Animation.PlayMode.NORMAL);
		
		idleAnimation = new Animation(1 / 3f, Assets.playerIdleTextures);
		idleAnimation.setPlayMode(Animation.PlayMode.LOOP);

		animatedBox2dSprite = new AnimatedBox2DSprite(new AnimatedSprite(
				idleAnimation));

		animatedBox2dSprite.setAdjustSize(false);
		animatedBox2dSprite.setUseOrigin(true);

		animatedBox2dSprite.setScale(1f / GameConstants.PIXELS_PER_METER);
		
		w = animatedBox2dSprite.getWidth();
		h = animatedBox2dSprite.getHeight();

		animatedBox2dSprite.setOrigin(w / 2, h / 2);

		FixtureDef fixDef = new FixtureDef();
		fixDef.density = 7f;
		fixDef.friction = 2f;

		shape = new PolygonShape();
		// shape.setAsBox(((w / 2) / GameConstants.PIXELS_PER_METER),
		// ((h / 4) / GameConstants.PIXELS_PER_METER));
		// shape.setAsBox(((w / 2) / GameConstants.PIXELS_PER_METER),
		// ((h / 3f) / GameConstants.PIXELS_PER_METER), new Vector2(0,.58f), 0);
		shape.setAsBox(((w / 2.5f) / GameConstants.PIXELS_PER_METER),
				((h / 2f) / GameConstants.PIXELS_PER_METER), new Vector2(0, 0),
				0);
		fixDef.shape = shape;
		body.createFixture(fixDef);


//		 CircleShape circleShape = new CircleShape();
//		 circleShape.setRadius((w / 2.5f) / GameConstants.PIXELS_PER_METER);
//		 circleShape.setPosition(new Vector2(0,
//		 (1 / GameConstants.PIXELS_PER_METER)
//		 + circleShape.getRadius() * 2.29f));
//		 fixDef.shape = circleShape;
//		 fixDef.friction = 2;
//		 fixDef.density = 5;
//		 body.createFixture(fixDef);
		 
			shape.setAsBox(((w / 2.5f) / GameConstants.PIXELS_PER_METER), (h / 6)
					/ GameConstants.PIXELS_PER_METER, new Vector2(0, .2f), 0);
			fixDef.density = 0;
			fixDef.friction = 0;
			fixDef.shape = shape;
			fixDef.isSensor = true;
			body.createFixture(fixDef).setUserData(
					FixtureUserData.PLAYER_FOOT_SENSOR);
		 
//		 circleShape.setRadius((w / 2.5f) / GameConstants.PIXELS_PER_METER);
//		 circleShape.setPosition(new Vector2(0,
//		 (1 / GameConstants.PIXELS_PER_METER)
//		 + circleShape.getRadius() * 2.4f));
//		 fixDef.shape = circleShape;
//		 fixDef.friction = 0;
//		 fixDef.density = 0;
//		 fixDef.isSensor = true;
//		 body.createFixture(fixDef).setUserData(
//					FixtureUserData.PLAYER_FOOT_SENSOR);

		// circleShape.setRadius((w / 2) / GameConstants.PIXELS_PER_METER);
		// circleShape.setPosition(new Vector2(0, (h * 1.5f)
		// / GameConstants.PIXELS_PER_METER));
		// fixDef.shape = circleShape;
		// body.createFixture(fixDef);

		shape.dispose();
		// circleShape.dispose();

		animatedBox2dSprite.setPosition(
				(-w / 2) + (Box2DUtils.width(body) / 2),
				(-h / 2) + (Box2DUtils.height(body) / 2));

		animatedBox2dSprite.flipFrames(false, true);

		body.setFixedRotation(true);
		body.setAngularDamping(2f);

		animatedBox2dSprite.play();

		body.setUserData(animatedBox2dSprite);

		float lightDistance = (Gdx.graphics.getWidth() / Gdx.graphics
				.getHeight()) * 6.5f;
		playerlight = new PointLight(rayHandler, 100, new Color(1, 1, 1, 0.2f),
				lightDistance, body.getPosition().x, body.getPosition().y);
		playerlight.attachToBody(body, Box2DUtils.width(body) / 2f,
				Box2DUtils.height(body) / 2f);
		
		Color lightColor = new Color(0.2f, 0.5f, 0.5f, 0.55f);


		flashLight = new FlashLight(rayHandler, lightColor, body);

		Light.setContactFilter(Globals.PLAYER_SENSOR_CATEGORY_BITS,
				Globals.PLAYER_SENSOR_MASK_BITS,
				Globals.PLAYER_SENSOR_MASK_BITS);

		jetPack = new JetPack(world, body);
	}

	public void update(float delta, Vector2 mouseWorldPos, int waterLevel) {
		animatedBox2dSprite.update(1 / 60f);
		
		if(!animatedBox2dSprite.isFlipY()){
			animatedBox2dSprite.flipFrames(false, true, false);
		}

		if (movingLeft() && movingRight()) {			
			movement.x = 0;
		} else {
			if (movingLeft()) {			
				if(lookDirection != LookDirection.LEFT){
					lookDirection = LookDirection.LEFT;							
				}
				
				if (Globals.numOfFootContacts > 0) {
					animatedBox2dSprite.setAnimation(runAnimation);
				}
				
				if (!animatedBox2dSprite.isFlipX()) {
					
					animatedBox2dSprite.flipFrames(true, false, false);
				}
				movement.x = -speed;				
			} else if (movingRight()) {
				if(lookDirection != LookDirection.RIGHT){
					lookDirection = LookDirection.RIGHT;						
				}		
				
				if (Globals.numOfFootContacts > 0) {
					animatedBox2dSprite.setAnimation(runAnimation);
				}
				
				if (animatedBox2dSprite.isFlipX()) {
					
					animatedBox2dSprite.flipFrames(true, false, false);
				}
				movement.x = speed;				
			} else {
				movement.x = 0;
				if(body.getLinearVelocity().x == 0){
					animatedBox2dSprite.setAnimation(idleAnimation);
				}
			}
			
			if (Globals.numOfFootContacts < 1) {
				animatedBox2dSprite.setAnimation(jumpAnimation);
			}
		}

		if (Gdx.input.isKeyPressed(Keys.UP) || Gdx.input.isKeyPressed(Keys.W)) {
			movement.y = -speed;
		} else if (Gdx.input.isKeyPressed(Keys.DOWN)
				|| Gdx.input.isKeyPressed(Keys.S)) {
			movement.y = speed;
		} else
			movement.y = 0;

		if (Globals.isAndroid)
			flashlightEnabled = true;

		if (Globals.lightsEnabled) {
			flashLight.setActive(isFlashlightEnabled());
		}

		flashLight.update(delta, getPos(), mouseWorldPos);
		if (!flashLight.hasBattery())
			flashlightEnabled = false;

		if ((int) body.getPosition().y > waterLevel) {
			body.setGravityScale(0);
			if (Gdx.input.isKeyPressed(Keys.SPACE)) {
				swim(0, -(speed * 2));
			} else {
				swim(movement.x * 2, movement.y * 2);
			}

			isUnderWater = true;
		} else {
			if (Gdx.input.isKeyPressed(Keys.SPACE)) {
				if (canUseJetpack) {
					jetpackMove(-11f, delta);
				}
			} else {
				isUsingJetpack = false;
			}
			body.setGravityScale(1);
			move(movement.x);
			isUnderWater = false;
		}

		if (Globals.isCurrentGameMultiplayer) {
			final PlayerPositionUpdateRequest req = new PlayerPositionUpdateRequest();
			req.x = body.getPosition().x;
			req.y = body.getPosition().y;
			NetworkClient.client.sendUDP(req);

			if (Gdx.input.isCursorCatched()) {
				final PlayerMouseUpdateRequest req2 = new PlayerMouseUpdateRequest();
				req2.mouseX = mouseWorldPos.x;
				req2.mouseY = mouseWorldPos.y;
				NetworkClient.client.sendUDP(req2);
			}
		}
	}

	private boolean movingLeft() {
		return Gdx.input.isKeyPressed(Keys.LEFT)
				|| Gdx.input.isKeyPressed(Keys.A);
	}

	private boolean movingRight() {
		return Gdx.input.isKeyPressed(Keys.RIGHT)
				|| Gdx.input.isKeyPressed(Keys.D);
	}

	public void jump() {
		animatedBox2dSprite.setAnimation(jumpAnimation);
		canUseJetpack = false;
		body.applyLinearImpulse(0, -4f, body.getWorldCenter().x,
				body.getWorldCenter().y, true);
	}

	public void jetpackMove(float y, float deltaTime) {
		body.applyForceToCenter(0, y, true);
		jetPack.update(deltaTime, body.getWorldCenter());
		isUsingJetpack = true;
	}

	public void swim(float mx, float my) {
		body.setLinearDamping(10f);
		if (mx != 0 || my != 0)
			body.applyForce(mx, my, body.getWorldCenter().x,
					body.getWorldCenter().y, true);

		float angleRad = (float) Math.atan2(my, mx);

		if (body.getAngle() < angleRad)
			body.setAngularVelocity(rotationSpeed);
		else if (body.getAngle() < -angleRad)
			body.setAngularVelocity(-rotationSpeed);
		else if (body.getAngle() != angleRad)
			body.setTransform(body.getPosition(), angleRad);
	}

	public void move(float mx) {
		if (mx != 0) {
			body.setLinearDamping(1f);
			body.applyForce(mx * 1.5f, 0, body.getWorldCenter().x,
					body.getWorldCenter().y, true);

			// body.getFixtureList().get(0).setFriction(.5f);
		} else {
			// body.setLinearDamping(0f);
			// body.getFixtureList().get(0).setFriction(2f);
		}

	}

	public void setGravityScale(float gravity) {
		body.setGravityScale(gravity);
	}

	// public void setPosition(float x, float y) {
	// isMoving = true;
	//
	// // pos.x += x;
	// // pos.y += y;
	//
	// // float angleRad = (float) Math.atan2(y, x);
	// //
	// // if (body.getAngle() < angleRad)
	// // body.setAngularVelocity(rotationSpeed);
	// // else if (body.getAngle() < -angleRad)
	// // body.setAngularVelocity(-rotationSpeed);
	// // else if(body.getAngle() != angleRad)
	// // body.setTransform(body.getPosition(), angleRad);
	//
	// }

	public void reset() {

	}

	public void rotate(float newAngle) {
		// float angleRad = (float) Math.atan2(pos.y - oldPos.y, pos.x -
		// oldPos.x);
		// if (body.getAngle() < angleRad) {
		// body.setAngularVelocity(rotationSpeed);
		// } else {
		// body.setTransform(body.getPosition(), angleRad);
		// }

	}

	public void stop() {
		// body.setLinearDamping(5f);
		// System.out.println(body.getLinearDamping());
		// body.setLinearVelocity(0, 0);
		body.setAngularVelocity(0);
		movement = Vector2.Zero;
		// moveDirection = LookDirection.NONE;
	}

	public void scale(float amount) {
		animatedBox2dSprite.scale(amount);
	}

	public void draw(SpriteBatch batch) {
		// batch.enableBlending();
		animatedBox2dSprite.draw(batch, body);

		if (isUsingJetpack) {
			jetPack.draw(batch);
		}
	}

	public AnimatedBox2DSprite getSprite() {
		return animatedBox2dSprite;
	}

	public Body getBody() {
		return body;
	}

	public Vector2 getPos() {
		return body.getPosition();
	}

	public FlashLight getFlashLight() {
		return flashLight;
	}

	public boolean isFlashlightEnabled() {
		return flashlightEnabled;
	}
	
	public JetPack getJetPack(){
		return jetPack;
	}

	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
		case Keys.LEFT:
		case Keys.A:
			if (!animatedBox2dSprite.isFlipX()) {
				animatedBox2dSprite.flipFrames(true, false, false);
			}
			break;
		case Keys.RIGHT:
		case Keys.D:
			if (animatedBox2dSprite.isFlipX()) {				
				animatedBox2dSprite.flipFrames(true, false, false);
			}
			break;
		case Keys.R:
			body.setAngularVelocity(5f);
			break;
		case Keys.F:
			if (flashLight.hasBattery()) {
				flashlightEnabled = !flashlightEnabled;
			} else {
				flashlightEnabled = false;
			}
			break;
		case Keys.SPACE:
			if (!isUnderWater) {
				if (Globals.numOfFootContacts > 0) {
					jump();
				} else {
					jetPack.reset();
					canUseJetpack = true;
				}
			}
		default:
			return false;
		}

		if (Globals.isCurrentGameMultiplayer) {
			final PlayerKeyDownUpdateRequest req = new PlayerKeyDownUpdateRequest();
			req.lastKeyDown = keycode;
			NetworkClient.client.sendUDP(req);
		}
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch (keycode) {
		case Keys.LEFT:
		case Keys.A:
			stop();
			break;
		case Keys.RIGHT:
		case Keys.D:
			stop();
			break;
		case Keys.UP:
			break;
		case Keys.DOWN:
			break;
		case Keys.SPACE:
			if (Globals.numOfFootContacts < 1)
				jetPack.reset();
			break;
		case Keys.R:
			body.setAngularVelocity(0);
			break;
		default:
			return false;
		}

		if (Globals.isCurrentGameMultiplayer) {
			final PlayerKeyUpUpdateRequest req = new PlayerKeyUpUpdateRequest();
			req.lastKeyUp = keycode;
			NetworkClient.client.sendUDP(req);
		}
		return true;
	}

	public void dispose() {
		jetPack.dispose();
	}
}
