package com.stone.opengl.advanced4_lighting.object;

import android.opengl.Matrix;

import com.stone.opengl.advanced4_lighting.util.Geometry;

import java.util.Random;

/**
 * desc     : 粒子(喷泉)发射器
 * author   : stone
 * homepage : http://stone86.top
 * email    : aa86799@163.com
 * time     : 2018/11/12 10 34
 */
public class ParticleShooter {

    private final Geometry.Point position;
    private final Geometry.Vector direction;
    private final int color;

    private final float angleVariance; //角度变化量，控制粒子的扩散
    private final float speedVariance; //速度变化量，控制粒子的速度

    private final Random random = new Random();

    private float[] rotationMatrix = new float[16];
    private float[] directionVector = new float[4];
    private float[] resultVector = new float[4];

//    public ParticleShooter(Geometry.Point position, Geometry.Vector direction, int color) {
//        this.position = position;
//        this.direction = direction;
//        this.color = color;
//    }

    public ParticleShooter(Geometry.Point position, Geometry.Vector direction, int color,
                           float angleVarianceInDegrees, float speedVariance) {
        this.position = position;
        this.direction = direction;
        this.color = color;
        this.angleVariance = angleVarianceInDegrees;
        this.speedVariance = speedVariance;

        directionVector[0] = direction.x;
        directionVector[1] = direction.y;
        directionVector[2] = direction.z;
    }

    public void addParticles(ParticleSystem particleSystem, float particleStartTime, int count) {
        /*for (int i = 0; i <count; i++) {
            particleSystem.addParticle(position, color, direction, particleStartTime);
        }*/

        for (int i = 0; i < count; i++) {
            //利用了随机数，创建旋转矩阵，以改变发射角度
            Matrix.setRotateEulerM(rotationMatrix, 0,
                    (random.nextFloat() - 0.5f) * angleVariance,
                    (random.nextFloat() - 0.5f) * angleVariance,
                    (random.nextFloat() - 0.5f) * angleVariance);

            //旋转旋转乘以 方向向量，得出结果 resultVector
            Matrix.multiplyMV(
                    resultVector, 0,
                    rotationMatrix, 0,
                    directionVector, 0);

            //调整速度分量至少为1以上
            float speedAdjustment = 1f + random.nextFloat() * speedVariance;

            //新的 方向向量
            Geometry.Vector thisDirection = new Geometry.Vector(
                    resultVector[0] * speedAdjustment,
                    resultVector[1] * speedAdjustment,
                    resultVector[2] * speedAdjustment);

            particleSystem.addParticle(position, color, thisDirection, particleStartTime);
        }
    }
}
