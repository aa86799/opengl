package com.stone.opengl.advanced2_Skybox.util;


/**
 * desc     : 几何图形
 * author   : stone
 * homepage : http://stone86.top
 * email    : aa86799@163.com
 * time     : 2018/11/10 17 39
 */
public class Geometry {

    public static class Point {
        public final float x, y, z;

        public Point(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Point translateY(float distance) {
            return new Point(x, y + distance, z);
        }

        public Point translate(Vector vector) {
            return new Point(
                    x + vector.x,
                    y + vector.y,
                    z + vector.z);
        }
    }

    public static class Circle {
        public final Point center;
        public final float radius;

        public Circle(Point center, float radius) {
            this.center = center;
            this.radius = radius;
        }

        public Circle scale(float scale) {
            return new Circle(center, radius * scale);
        }
    }

    //圆柱体: 就像一个扩展的圆，有一个中心、一个半径、一个高度
    public static class Cylinder {
        public final Point center;
        public final float radius;
        public final float height;

        public Cylinder(Point center, float radius, float height) {
            this.center = center;
            this.radius = radius;
            this.height = height;
        }
    }

    //向量，有方向
    public static class Vector  {
        public final float x, y, z;

        public Vector(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public float length() {
            return (float) Math.sqrt(x * x + y * y + z * z);
        }

        // http://en.wikipedia.org/wiki/Cross_product
        //计算两个向量的交叉乘积；得到第三个向量
        public Vector crossProduct(Vector other) {
            return new Vector(
                    (y * other.z) - (z * other.y),
                    (z * other.x) - (x * other.z),
                    (x * other.y) - (y * other.x));
        }
        // http://en.wikipedia.org/wiki/Dot_product
        //计算两个向量的点乘积；
        public float dotProduct(Vector other) {
            return x * other.x
                    + y * other.y
                    + z * other.z;
        }

        //缩放向量的分量
        public Vector scale(float f) {
            return new Vector(
                    x * f,
                    y * f,
                    z * f);
        }
    }

    //射线
    public static class Ray {
        public final Point point;
        public final Vector vector;

        public Ray(Point point, Vector vector) {
            this.point = point;
            this.vector = vector;
        }
    }

    //球体
    public static class Sphere {
        public final Point center; //球心点
        public final float radius;

        public Sphere(Point center, float radius) {
            this.center = center;
            this.radius = radius;
        }
    }

    //平面
    public static class Plane {
        public final Point point;
        public final Vector normal; //垂直于平面上的一个向量

        public Plane(Point point, Vector normal) {
            this.point = point;
            this.normal = normal;
        }
    }

    public static Vector vectorBetween(Point from, Point to) {
        return new Vector(
                to.x - from.x,
                to.y - from.y,
                to.z - from.z);
    }

    //球体与射线是否相交
    public static boolean intersects(Sphere sphere, Ray ray) {
        //球体中心点与射线两点构成一个虚拟三角形，
        //判断球体中心点到三角形的高，是否 小于 球体的半径， 若小则相交
        return distanceBetween(sphere.center, ray) < sphere.radius;
    }

    // http://mathworld.wolfram.com/Point-LineDistance3-Dimensional.html
    // Note that this formula treats Ray as if it extended infinitely past
    // either point.
    public static float distanceBetween(Point point, Ray ray) {
        //射线起点到球体中心点的向量
        Vector p1ToPoint = vectorBetween(ray.point, point);
        //translate中 将点坐标加上向量，得到远点的坐标；再经由 vectorBetween()，得到射线远点到球体中心点的向量
        Vector p2ToPoint = vectorBetween(ray.point.translate(ray.vector), point);

        // The length of the cross product gives the area of an imaginary
        // parallelogram having the two vectors as sides. A parallelogram can be
        // thought of as consisting of two triangles, so this is the same as
        // twice the area of the triangle defined by the two vectors.
        // http://en.wikipedia.org/wiki/Cross_product#Geometric_meaning
        /* 计算两个向量的交叉乘积；得到第三个向量；这个向量的长度会是前两个向量定义的三角形面积的两倍 */
        float areaOfTriangleTimesTwo = p1ToPoint.crossProduct(p2ToPoint).length();//长度
        float lengthOfBase = ray.vector.length();

        // The area of a triangle is also equal to (base * height) / 2. In
        // other words, the height is equal to (area * 2) / base. The height
        // of this triangle is the distance from the point to the ray.
        //根据三角形面积公式：1/2* a*h=S;   现在 areaOfTriangleTimesTwo = 2S, 所以 h=如下
        float distanceFromPointToRay = areaOfTriangleTimesTwo / lengthOfBase;
        return distanceFromPointToRay;
    }

    // http://en.wikipedia.org/wiki/Line-plane_intersection
    // This also treats rays as if they were infinite. It will return a
    // point full of NaNs if there is no intersection point.
    // 射线与桌子平面是否相交
    public static Point intersectionPoint(Ray ray, Plane plane) {
        //平面与射线间的向量
        Vector rayToPlaneVector = vectorBetween(ray.point, plane.point);

        //两两向量点积 相除，得出缩放因子。
        float scaleFactor = rayToPlaneVector.dotProduct(plane.normal)
                / ray.vector.dotProduct(plane.normal);

        //相交点
        Point intersectionPoint = ray.point.translate(ray.vector.scale(scaleFactor));
        return intersectionPoint;
    }
}
