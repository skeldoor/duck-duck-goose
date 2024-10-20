package com.skeldoor;
// Code adapted from https://github.com/Mrnice98/Fake-Pet-Plugin
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.geometry.SimplePolygon;
import net.runelite.api.model.Jarvis;

import java.util.Random;

import static net.runelite.api.Perspective.COSINE;
import static net.runelite.api.Perspective.SINE;

public class Duck {

    private enum POSE_ANIM
    {
        IDLE,
        WALK,
    }

    private Client client;
    private RuneLiteObject rlObject;
    DuckPond pond;
    private int cTargetIndex;

    private class Target
    {
        public WorldPoint worldDestinationPosition;
        public LocalPoint localDestinationPosition;
        public int currentDistance;
    }

    private final int MAX_TARGET_QUEUE_SIZE = 10;
    private final Target[] targetQueue = new Target[MAX_TARGET_QUEUE_SIZE];
    private int targetQueueSize;
    private int lastDistance;
    int currentMovementSpeed = 0;
    int currentAnimationID = 6818;
    int normalDuckIdleAnimationId = 6818;
    int normalDuckMovingAnimationId = 6817;
    int colosseumDuckIdleAnimationId = 6813;
    int colosseumDuckMovingAnimationId = 6819;
    public Animation[] animationPoses = new Animation[2];
    private final int[] duckModelIds = {26873, 26870};
    private final int[] colosseumDuckModelIds = {26873, 26870};
    private final String[] duckNames = {"Drake", "Duck"};
    private String duckName;
    private boolean quacking = false;
    private int quackTimer = 0;
    private final int MAX_QUACK_TIME = 45;
    private final int COLOSSEUM_MAX_QUACK_TIME = 180;
    private final String defaultQuackText = "Quack!";
    private String quackText = "Quack!";
    private final String[] colosseumEncouragementText = {
            "Let's get quacking! You can do this!",
            "Waddle to victory, my friend!",
            "You're the ultimate quack attack!",
            "Flap harder, fight stronger!",
            "Be like water off a duck's back, unshakable and smooth!",
            "Don't just paddle, make waves!",
            "Feather you're ready or not, you can wing this!",
            "Stay in formation, we're flying to victory!",
            "Quack down the obstacles, you've got this!",
            "Keep your beak sharp and your eyes on the prize!",
            "Duck and cover!",
            "Feather your nest with wins!",
            "Just wing it!",
            "Dive in, you'll float!",
            "Paddle through the pain!",
            "Stay quack-tastic!",
            "Fly high, duckling!",
            "Shake your tail feathers!",
            "Quack up the courage!",
            "Beak-lieve in yourself!",
            "Splash into action!",
            "Ruffle some feathers!",
            "Quack on, warrior!",
            "Bill them with bravery!",
            "Migrate to victory!",
            "Why are you listening to encouragement from a duck?"};
    private Boolean isColosseumDuck = false;
    private int sex;

    SimplePolygon clickbox;

    public void init(Client client, DuckPond pond, Boolean isColosseumDuck)
    {
        this.client = client;
        this.rlObject = client.createRuneLiteObject();
        this.pond = pond;
        this.isColosseumDuck = isColosseumDuck;
        assignDuckSex();
        setupDuckNameModelAnimation();
        for (int i = 0; i < MAX_TARGET_QUEUE_SIZE; i++)
            targetQueue[i] = new Target();
    }

    private void setupDuckNameModelAnimation(){
        if (this.isColosseumDuck){
            setModel(client.loadModel(colosseumDuckModelIds[this.sex]));
            this.animationPoses[POSE_ANIM.IDLE.ordinal()] = client.loadAnimation(colosseumDuckIdleAnimationId);
            this.animationPoses[POSE_ANIM.WALK.ordinal()] = client.loadAnimation(colosseumDuckMovingAnimationId);
        }else {
            setModel(client.loadModel(duckModelIds[this.sex]));
            this.animationPoses[POSE_ANIM.IDLE.ordinal()] = client.loadAnimation(normalDuckIdleAnimationId);
            this.animationPoses[POSE_ANIM.WALK.ordinal()] = client.loadAnimation(normalDuckMovingAnimationId);
        }
        this.duckName = duckNames[this.sex];
    }

    // Winner for weirdest function name I've ever written
    private void assignDuckSex(){
        // I figured like 1 in 4 ducks are boy ducks? idk?
        int random = getRandom(0, 4);
        if (random == 0){
            this.sex = 0;
        } else {
            this.sex = 1;
        }
    }

    public void setModel(Model model)
    {
        rlObject.setModel(model);
    }

    public RuneLiteObject getRlObject(){
        return rlObject;
    }

    public String getDuckName(){
        return duckName;
    }

    public void spawn(WorldPoint position, int jauOrientation)
    {
        LocalPoint localPosition = LocalPoint.fromWorld(client, pond.getRandomPointInPond());
        if (localPosition != null && client.getPlane() == position.getPlane()){
            rlObject.setLocation(localPosition, position.getPlane());
        }
        else {
            return;
        }
        rlObject.setOrientation(jauOrientation);
        rlObject.setAnimation(animationPoses[0]);
        rlObject.setShouldLoop(true);
        rlObject.setActive(true);
        this.currentAnimationID = animationPoses[0].getId();
        this.currentMovementSpeed = 0;
        this.targetQueueSize = 0;
    }

    public void despawn()
    {
        rlObject.setActive(false);
        this.currentAnimationID = -1;
        this.currentMovementSpeed = 0;
        this.targetQueueSize = 0;
    }

    public LocalPoint getLocalLocation()
    {
        return rlObject.getLocation();
    }

    public boolean isActive()
    {
        return rlObject.isActive();
    }

    public int getOrientation()
    {
        return rlObject.getOrientation();
    }

    public SimplePolygon getClickbox(){
        return clickbox;
    }

    public boolean getQuacking(){
        return quacking;
    }

    public String getQuackText(){
       return quackText;
    }

    public void quack(Boolean silenceDucks, Boolean onlyEnourage){
        if (silenceDucks) {
            quacking = false;
            return;
        }
        quacking = true;
        
        if (isColosseumDuck && onlyEnourage){
            this.quackText = colosseumEncouragementText[new java.util.Random().nextInt(colosseumEncouragementText.length)];
            quackTimer = COLOSSEUM_MAX_QUACK_TIME;
            return;
        }
        
        if (isColosseumDuck && new java.util.Random().nextInt(30) == 0){
            this.quackText = colosseumEncouragementText[new java.util.Random().nextInt(colosseumEncouragementText.length)];
            quackTimer = COLOSSEUM_MAX_QUACK_TIME;
        } else {
            quackTimer = MAX_QUACK_TIME;
            this.quackText = defaultQuackText;
        }
    }

    public String getExamine(String menuTarget){
        String[] duckExamines = {"Quack?", "It walks like a duck. Well, I guess it waddles like one."};
        String rareDrakeExamine = "This isn't Josh?";
        String duckExamine;
        if (menuTarget.contains("Drake")){
            duckExamine = duckExamines[0];
            if (getRandom(0, 50) == 0) duckExamine = rareDrakeExamine;
        } else {
            duckExamine = duckExamines[1];
        }
        return duckExamine;
    }

    // moveTo() adds target movement states to the queue for later per-frame updating for rendering in onClientTick()
    public void moveTo(WorldPoint worldPosition, int jauOrientation)
    {

        if (!rlObject.isActive())
        {
            spawn(worldPosition, jauOrientation);
        }

        LocalPoint localPosition = LocalPoint.fromWorld(client, worldPosition);

        // just clear the queue and move immediately to the destination if many ticks behind
        if (targetQueueSize >= MAX_TARGET_QUEUE_SIZE - 2)
        {
            targetQueueSize = 0;
        }


        int prevTargetIndex = (cTargetIndex + targetQueueSize - 1) % MAX_TARGET_QUEUE_SIZE;
        int newTargetIndex = (cTargetIndex + targetQueueSize) % MAX_TARGET_QUEUE_SIZE;

        if (localPosition == null)
        {
            return;
        }

        WorldPoint prevWorldPosition;
        if (targetQueueSize++ > 0)
        {
            prevWorldPosition = targetQueue[prevTargetIndex].worldDestinationPosition;
        }
        else
        {
            prevWorldPosition = WorldPoint.fromLocal(client,rlObject.getLocation());
        }

        int distance = prevWorldPosition.distanceTo(worldPosition);

        this.targetQueue[newTargetIndex].worldDestinationPosition = worldPosition;
        this.targetQueue[newTargetIndex].localDestinationPosition = localPosition;
        this.targetQueue[newTargetIndex].currentDistance = distance;

    }


    public void onClientTick()
    {
        if (quackTimer > 0) quackTimer--;
        if (quackTimer == 0) quacking = false;
        if (rlObject.isActive())
        {
            if (targetQueueSize > 0)
            {
                if (targetQueue[cTargetIndex] == null || targetQueue[cTargetIndex].worldDestinationPosition == null) return;
                int targetPlane = targetQueue[cTargetIndex].worldDestinationPosition.getPlane();

                LocalPoint targetPosition = targetQueue[cTargetIndex].localDestinationPosition;

                if (targetPosition == null){
                    despawn();
                    return;
                }

                double intx = rlObject.getLocation().getX() - targetPosition.getX();
                double inty = rlObject.getLocation().getY() - targetPosition.getY();

                boolean rotationDone = rotateObject(intx,inty);

                if (client.getPlane() != targetPlane || !targetPosition.isInScene())
                {
                    // this actor is no longer in a visible area on our client, so let's despawn it
                    despawn();
                    return;
                }

                //apply animation if move-speed / distance has changed
                if (lastDistance != targetQueue[cTargetIndex].currentDistance)
                {
                    int distance = targetQueue[cTargetIndex].currentDistance;

                    // we don't want to go beyond walk (speed of 1)
                    rlObject.setAnimation(distance > 1 ? null : animationPoses[distance]);

                    if (rlObject.getAnimation() == null)
                    {
                        rlObject.setAnimation(animationPoses[1]);
                    }

                }

                this.lastDistance = targetQueue[cTargetIndex].currentDistance;

                LocalPoint currentPosition = rlObject.getLocation();
                int dx = targetPosition.getX()  - currentPosition.getX();
                int dy = targetPosition.getY() - currentPosition.getY();


                // are we not where we need to be?
                if (dx != 0 || dy != 0)
                {

                    int speed = 2;
                    // only use the delta if it won't send up past the target
                    if (Math.abs(dx) > speed)
                    {
                        dx = Integer.signum(dx) * speed;
                    }

                    if (Math.abs(dy) > speed)
                    {
                        dy = Integer.signum(dy) * speed;
                    }




                    LocalPoint newLocation = new LocalPoint(currentPosition.getX() + dx , currentPosition.getY() + dy);


                    rlObject.setLocation(newLocation, targetPlane);

                    dx = targetPosition.getX() - rlObject.getLocation().getX();
                    dy = targetPosition.getY() - rlObject.getLocation().getY();
                }



                // have we arrived at our target?
                if (dx == 0 && dy == 0 && rotationDone)
                {
                    // if so, pull out the next target
                    cTargetIndex = (cTargetIndex + 1) % MAX_TARGET_QUEUE_SIZE;
                    targetQueueSize--;
                    rlObject.setAnimation(animationPoses[0]);
                }

            }

            LocalPoint lp = getLocalLocation();
            int zOff = Perspective.getTileHeight(client, lp, client.getPlane());

            clickbox = calculateAABB(client, getRlObject().getModel(), getOrientation(), lp.getX(), lp.getY(), client.getPlane(), zOff);

        }

    }

    public boolean rotateObject(double intx, double inty)
    {

        final int JAU_FULL_ROTATION = 2048;
        int targetOrientation = radToJau(Math.atan2(intx, inty));
        int currentOrientation = rlObject.getOrientation();

        int dJau = (targetOrientation - currentOrientation) % JAU_FULL_ROTATION;

        if (dJau != 0)
        {
            final int JAU_HALF_ROTATION = 1024;
            final int JAU_TURN_SPEED = 32;
            int dJauCW = Math.abs(dJau);

            if (dJauCW > JAU_HALF_ROTATION)// use the shortest turn
            {
                dJau = (currentOrientation - targetOrientation) % JAU_FULL_ROTATION;
            }

            else if (dJauCW == JAU_HALF_ROTATION)// always turn right when turning around
            {
                dJau = dJauCW;
            }


            // only use the delta if it won't send up past the target
            if (Math.abs(dJau) > JAU_TURN_SPEED)
            {
                dJau = Integer.signum(dJau) * JAU_TURN_SPEED;
            }


            int newOrientation = (JAU_FULL_ROTATION + rlObject.getOrientation() + dJau) % JAU_FULL_ROTATION;

            rlObject.setOrientation(newOrientation);

            dJau = (targetOrientation - newOrientation) % JAU_FULL_ROTATION;
        }

        return dJau == 0;
    }

    static int radToJau(double a)
    {
        int j = (int) Math.round(a / Perspective.UNIT);
        return j & 2047;
    }

    public int getRandom(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    private static SimplePolygon calculateAABB(Client client, Model m, int jauOrient, int x, int y, int z, int zOff)
    {
        AABB aabb = m.getAABB(jauOrient);

        int x1 = aabb.getCenterX();
        int y1 = aabb.getCenterZ();
        int z1 = aabb.getCenterY() + zOff;

        int ex = aabb.getExtremeX();
        int ey = aabb.getExtremeZ();
        int ez = aabb.getExtremeY();

        int x2 = x1 + ex;
        int y2 = y1 + ey;
        int z2 = z1 + ez;

        x1 -= ex;
        y1 -= ey;
        z1 -= ez;

        int[] xa = new int[]{
                x1, x2, x1, x2,
                x1, x2, x1, x2
        };
        int[] ya = new int[]{
                y1, y1, y2, y2,
                y1, y1, y2, y2
        };
        int[] za = new int[]{
                z1, z1, z1, z1,
                z2, z2, z2, z2
        };

        int[] x2d = new int[8];
        int[] y2d = new int[8];

        modelToCanvasCpu(client, 8, x, y, z, 0, xa, ya, za, x2d, y2d);

        return Jarvis.convexHull(x2d, y2d);
    }

    private static void modelToCanvasCpu(Client client, int end, int x3dCenter, int y3dCenter, int z3dCenter, int rotate, int[] x3d, int[] y3d, int[] z3d, int[] x2d, int[] y2d)
    {
        final int
                cameraPitch = client.getCameraPitch(),
                cameraYaw = client.getCameraYaw(),

                pitchSin = SINE[cameraPitch],
                pitchCos = COSINE[cameraPitch],
                yawSin = SINE[cameraYaw],
                yawCos = COSINE[cameraYaw],
                rotateSin = SINE[rotate],
                rotateCos = COSINE[rotate],

                cx = x3dCenter - client.getCameraX(),
                cy = y3dCenter - client.getCameraY(),
                cz = z3dCenter - client.getCameraZ(),

                viewportXMiddle = client.getViewportWidth() / 2,
                viewportYMiddle = client.getViewportHeight() / 2,
                viewportXOffset = client.getViewportXOffset(),
                viewportYOffset = client.getViewportYOffset(),

                zoom3d = client.getScale();

        for (int i = 0; i < end; i++)
        {
            int x = x3d[i];
            int y = y3d[i];
            int z = z3d[i];

            if (rotate != 0)
            {
                int x0 = x;
                x = x0 * rotateCos + y * rotateSin >> 16;
                y = y * rotateCos - x0 * rotateSin >> 16;
            }

            x += cx;
            y += cy;
            z += cz;

            final int
                    x1 = x * yawCos + y * yawSin >> 16,
                    y1 = y * yawCos - x * yawSin >> 16,
                    y2 = z * pitchCos - y1 * pitchSin >> 16,
                    z1 = y1 * pitchCos + z * pitchSin >> 16;

            int viewX, viewY;

            if (z1 < 50)
            {
                viewX = Integer.MIN_VALUE;
                viewY = Integer.MIN_VALUE;
            }
            else
            {
                viewX = (viewportXMiddle + x1 * zoom3d / z1) + viewportXOffset;
                viewY = (viewportYMiddle + y2 * zoom3d / z1) + viewportYOffset;
            }

            x2d[i] = viewX;
            y2d[i] = viewY;
        }
    }
}
