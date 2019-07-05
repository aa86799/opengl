/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
***/
package com.stone.opengl.advanced1_particles.object;

import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.glDrawArrays;
import android.graphics.Color;

import com.stone.opengl.advanced1_particles.constant.Constants;
import com.stone.opengl.advanced1_particles.data.VertexArray;
import com.stone.opengl.advanced1_particles.program.ParticleShaderProgram;
import com.stone.opengl.advanced1_particles.util.Geometry;

/**
 * 粒子系统
 */
public class ParticleSystem {
    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int VECTOR_COMPONENT_COUNT = 3;    
    private static final int PARTICLE_START_TIME_COMPONENT_COUNT = 1;

    private static final int TOTAL_COMPONENT_COUNT = 
        POSITION_COMPONENT_COUNT
      + COLOR_COMPONENT_COUNT 
      + VECTOR_COMPONENT_COUNT      
      + PARTICLE_START_TIME_COMPONENT_COUNT;

    private static final int STRIDE = TOTAL_COMPONENT_COUNT * Constants.BYTES_PER_FLOAT;

    private final float[] particles; //粒子数组
    private final VertexArray vertexArray;
    private final int maxParticleCount;

    private int currentParticleCount;
    private int nextParticle;

    public ParticleSystem(int maxParticleCount) {
        particles = new float[maxParticleCount * TOTAL_COMPONENT_COUNT];
        vertexArray = new VertexArray(particles);
        this.maxParticleCount = maxParticleCount;
    }
    
    public void addParticle(Geometry.Point position, int color, Geometry.Vector direction,
                            float particleStartTime) {
        final int particleOffset = nextParticle * TOTAL_COMPONENT_COUNT;
		
        int currentOffset = particleOffset;
        nextParticle++;
        
        if (currentParticleCount < maxParticleCount) {
            currentParticleCount++;
        }

        //若为最大数，从0开始
        if (nextParticle == maxParticleCount) {
            // Start over at the beginning, but keep currentParticleCount so
            // that all the other particles still get drawn.
            nextParticle = 0;
        }  
        
        particles[currentOffset++] = position.x;
        particles[currentOffset++] = position.y;
        particles[currentOffset++] = position.z;
        
        particles[currentOffset++] = Color.red(color) / 255f;
        particles[currentOffset++] = Color.green(color) / 255f;
        particles[currentOffset++] = Color.blue(color) / 255f;
        //方向向量
        particles[currentOffset++] = direction.x;
        particles[currentOffset++] = direction.y;
        particles[currentOffset++] = direction.z;             
        
        particles[currentOffset++] = particleStartTime;

        vertexArray.updateBuffer(particles, particleOffset, TOTAL_COMPONENT_COUNT);
    }

    public void bindData(ParticleShaderProgram particleProgram) {
        int dataOffset = 0;
        vertexArray.setVertexAttribPointer(dataOffset,
            particleProgram.getPositionLocation(),
            POSITION_COMPONENT_COUNT, STRIDE);
        dataOffset += POSITION_COMPONENT_COUNT;
        
        vertexArray.setVertexAttribPointer(dataOffset,
            particleProgram.getColorLocation(),
            COLOR_COMPONENT_COUNT, STRIDE);        
        dataOffset += COLOR_COMPONENT_COUNT;
        
        vertexArray.setVertexAttribPointer(dataOffset,
            particleProgram.getDirectionVectorLocation(),
            VECTOR_COMPONENT_COUNT, STRIDE);
        dataOffset += VECTOR_COMPONENT_COUNT;       
        
        vertexArray.setVertexAttribPointer(dataOffset,
            particleProgram.getParticleStartTimeLocation(),
            PARTICLE_START_TIME_COMPONENT_COUNT, STRIDE);
    }

    public void draw() {
        // parameter - count : 绘制时，从顶点数组中查找进行绘制顶点数量
        glDrawArrays(GL_POINTS, 0, currentParticleCount);
    }
}
