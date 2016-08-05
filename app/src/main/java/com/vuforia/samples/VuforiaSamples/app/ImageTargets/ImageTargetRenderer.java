/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.vuforia.samples.VuforiaSamples.app.ImageTargets;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.vuforia.Matrix44F;
import com.vuforia.Renderer;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Trackable;
import com.vuforia.TrackableResult;
import com.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.vuforia.Vuforia;
import com.vuforia.samples.SampleApplication.SampleApplicationSession;
import com.vuforia.samples.SampleApplication.utils.CubeObject;
import com.vuforia.samples.SampleApplication.utils.model;
import com.vuforia.samples.SampleApplication.utils.CubeShaders;
import com.vuforia.samples.SampleApplication.utils.LoadingDialogHandler;
import com.vuforia.samples.SampleApplication.utils.SampleApplication3DModel;
import com.vuforia.samples.SampleApplication.utils.SampleUtils;
import com.vuforia.samples.SampleApplication.utils.Teapot;
import com.vuforia.samples.SampleApplication.utils.Texture;

public class ImageTargetRenderer implements GLSurfaceView.Renderer
{
    private static final String LOGTAG = "ImageTargetRenderer";

    private SampleApplicationSession vuforiaAppSession;
    private ImageTargets mActivity;

    private Vector<Texture> mTextures;

    private int shaderProgramID;
    private int shaderProgramID2;

    private int vertexHandle;

    private int normalHandle;

    private int textureCoordHandle;

    private int mvpMatrixHandle;

    private int texSampler2DHandle;

    private Teapot mTeapot;
    private CubeObject mCubeObject;
    private float kBuildingScale = 12.0f;
    private SampleApplication3DModel mBuildingsModel;

    private Renderer mRenderer;

    boolean mIsActive = false;

    private static final float OBJECT_SCALE_FLOAT = 5.0f;
    private static final float OBJECT_SCALE_Z = 1.0f;
    private static final float OBJECT_SCALE_FLOATUP = 200f;
    private float  AnimationZ=-5000.0f;
    private float  AnimationTZ=5400.0f;
    private float  AnimationRZ=0f;
    private float  AnimationFZ=-50f;
    boolean TeapotAppear=false;


    public ImageTargetRenderer(ImageTargets activity,
                               SampleApplicationSession session)
    {
        Log.i(LOGTAG,"creat a ImageTargetRenderer");
        mActivity = activity;
        vuforiaAppSession = session;
    }


    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl)
    {
        if (!mIsActive)
            return;

        // Call our function to render content
        renderFrame();
    }


    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");

        initRendering();

        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();
    }


    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");

        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);
    }

    // Function for initializing the renderer.
    private void initRendering()
    {
        mTeapot = new Teapot();
        mCubeObject = new CubeObject();
        Log.i(LOGTAG,"initRendering");
        mRenderer = Renderer.getInstance();

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
                : 1.0f);
        GLES20.glClearColor(0,0,0,1.0f); //设置屏幕背景色RGBA
   /*     Android 上使用Opengl进行滤镜渲染效率较高，
        比起单纯的使用CPU给用户带来的体验会好很多。
        滤镜的对象是图片，图片是以Bitmap的形式表示，Opengl不能直接处理Bitmap，
        在Android上一般是通过GLSurfaceView来进行渲染的，
        也可以说成Android需要借助GLSurfaceView来完成对图片的渲染。*/
/*　GlSurfaceView 的图片来源依然是Bitmap，但是Bitmap需要以纹理（Texture）的形式载入到Opengl中。
一下为载入纹理的步骤：
        　　1. GLES20.glGenTextures() : 生成纹理资源的句柄
         void glGenTextures(GLsizei n, GLuint *textures)
        参数说明：
                  n：用来生成纹理的数量（下面为 1）
            　　textures：存储纹理索引的（下面为一个数组 t.mTextureID）
        //句柄的学习http://www.cppblog.com/mymsdn/archive/2012/06/20/74221.html
        　　2. GLES20.glBindTexture(): 绑定句柄
       　　 3. GLUtils.texImage2D() ：将bitmap传递到已经绑定的纹理中
        　　4. GLES20.glTexParameteri() ：设置纹理属性，过滤方式，拉伸方式等*/
        //GLES的API文档https://www.opengl.org/wiki/Category:Core_API_Reference
        //函数思考学习http://blog.csdn.net/shuaihj/article/details/7244320
        //以下的函数都会在上面网站找到
        for (Texture t : mTextures)
        {
            GLES20.glGenTextures(1, t.mTextureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            //这是纹理过滤，MIN,LINEAR缩小线性过滤，线性(使用距离当前渲染像素中心最近的4个纹素加权平均值.)
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            //放大线性过滤,后面的LINEAR可以更换为NEAREST接近滤波
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                    t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, t.mData);
            //  SampleUtils.checkGLError("");查看util下的ampleSUtils类，该函数用于检查GLES运行时的错误
            //定义一个二维纹理映射。 glTexImage2D(GLenum target,GLint level,GLint components,
            // GLsizei width, glsizei height,GLint border,GLenum format,
            // GLenum type, const GLvoid *pixels);
            //参数format和type描述了纹理映射的格式和数据类型
            // 篇幅过长避免混乱，链接http://blog.csdn.net/shuaihj/article/details/7244313
        }
        //下面这个SampleUtils类里面有相应的注释，观察下面的CubeShaders.CUBE_MESH_VERTEX_SHADER,
        //查看可知这是一个String类型数据，仔细观察可知这是一个程序的Scr文件，
        //OpenGL会自行去调用里面的程序进行运行
        shaderProgramID = SampleUtils.createProgramFromShaderSrc(
                CubeShaders.CUBE_MESH_VERTEX_SHADER,
                CubeShaders.CUBE_MESH_FRAGMENT_SHADER);

        // glGetAttribLocation方法：获取着色器程序中，指定为attribute类型变量的id
        // // 获取指向着色器中vertexPosition的index
        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexPosition");//attribute vec4 vertexPosition
        normalHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexNormal");//attribute vec4 vertexNormal
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexTexCoord");//attribute vec2 vertexTexCoord
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID,
                "modelViewProjectionMatrix");//uniform mat4 modelViewProjectionMatrix
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID,
                "texSampler2D");//uniform sampler2D texSampler2D
       /* SampleUtils.checkGLError("");*/
        initRenderingTeapot();
        try
        {
            mBuildingsModel = new SampleApplication3DModel();
            mBuildingsModel.loadModel(mActivity.getResources().getAssets(),
                    "ImageTargets/Buildings.txt");
        } catch (IOException e)
        {
            Log.e(LOGTAG, "Unable to load buildings");
        }

        // Hide the Loading Dialog
        mActivity.loadingDialogHandler
                .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);

    }


    // The render function.
    private void renderFrame() {
        //Log.i(LOGTAG,"renderFrame");
        //这个画图功能函数也在不断的调用
        //清除颜色缓冲和深度缓冲
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        State state = mRenderer.begin();
        mRenderer.drawVideoBackground();

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // Set the viewport
        int[] viewport = vuforiaAppSession.getViewport();
        GLES20.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);

        // handle face culling, we need to detect if we are using reflection
        // to determine the direction of the culling
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
            GLES20.glFrontFace(GLES20.GL_CW); // Front camera
        else
            GLES20.glFrontFace(GLES20.GL_CCW); // Back camera

        // did we find any trackables this frame?



        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++)
        {
            if (tIdx == 0) {
                //以下为追踪到的结果，http://bbs.csdn.net/topics/390870561
                TrackableResult result = state.getTrackableResult(tIdx);
                Trackable trackable = result.getTrackable();
                // trackable为每个可跟踪的内容，具有名字，id和类型
                printUserData(trackable);
                Matrix44F modelViewMatrix_Vuforia = Tool
                        .convertPose2GLMatrix(result.getPose());
                // 接下来就获取姿态矩阵等，为一个4*4的矩阵，有四个表示坐标的行向量
                //姿态矩阵又称方向余弦矩阵
                //http://wenku.baidu.com/link?url=exe-qKW5y5vlczYRrhfdYm30S1batlszVGf1gwTtcUHUih-drophAUDnCAc4AM-BMIOPTtjfj_tcrxPRNkbsgvgLVAuetH2nMU2SWkHmh1_
                float[] modelViewMatrix= modelViewMatrix_Vuforia.getData();
                float[] modelViewMatrix2=new float[16];

                /*float[] modelViewMatrix = {
                        1.0f, 0, 0, 0,
                        0, 1.0f, 0, 0,
                        0, 0, -1.0f, 0,
                        0, 0, 1000.0f, 1
                };*/
                //？yes no 进行判断，如果为stones为0，tarmac为2，都不是为1
                //在ImageTargets方法的LoadTexture中可以发现
                //0为grass,1为bule,2为red,以下对应的正是纹理图片的选择，修改测试成功
                Log.i(LOGTAG,"draw cube");
                for (int i = 0; i < 16; i += 4) {

                    Log.i(LOGTAG, modelViewMatrix[i] + "   " + modelViewMatrix[i + 1] + "   "
                            + modelViewMatrix[i + 2] + "   " + modelViewMatrix[i + 3] + "   ");
                }

                //int textureIndex = 3;
                int textureIndex = trackable.getName().equalsIgnoreCase("stones") ? 3
                        : 2;
                textureIndex = trackable.getName().equalsIgnoreCase("tarmac") ? 1
                        : textureIndex;
                //tracker包含chip，如果不是上述二者则为chip
                // deal with the modelview and projection matrices
                float[] modelViewProjection = new float[16];

                //以下关于矩阵的变换函数的部分介绍
                // http://blog.sina.com.cn/s/blog_a23d30f101018gl4.html
                if (!mActivity.isExtendedTrackingActive()) {
                    //对茶壶数据矩阵做变换
                    if (AnimationZ <= 200.0f) {
                        Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f,
                                AnimationZ);
                        AnimationZ += 20.0f;
                        Matrix.rotateM(modelViewMatrix, 0, AnimationRZ, 0, 1.0f, 0);
                        AnimationRZ += 10.0f;
                    } else {
                        Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f,
                                100.0f);
                        TeapotAppear = true;
                    }
                    modelViewMatrix2 =modelViewMatrix.clone();
                    //此处若直接画等号会出现茶壶过大的BUG
                    if(TeapotAppear == true) {
                        Log.i(LOGTAG, "Matrix");
                        for (int i = 0; i < 16; i += 4) {

                            Log.i(LOGTAG, modelViewMatrix2[i] + "   " + modelViewMatrix2[i + 1] + "   "
                                    + modelViewMatrix2[i + 2] + "   " + modelViewMatrix2[i + 3] + "   ");
                        }
                    }
                    //物体平移Matrix.translateM(mMMatrix,0,//偏移量,x, y, z//平移量)
                    //将物体沿着Z轴上升
                    //
                    Matrix.scaleM(modelViewMatrix, 0, 200.0f,
                            200.0f, 1.0f);

                    //Matrix.scaleM(mMMatrix,sx,sy, sz//缩放因子)
                    //http://www.360doc.com/content/14/1028/09/19175681_420513219.shtml相机的学习
                } else {
                    //l楼房矩阵变换
                    // Matrix.rotateM(modelViewMatrix, 0, 90.0f, 1.0f, 0, 0);
                    //Matrix.rotateM(mMMatrix,0,//偏移量angle,//旋转角度x, y, z//需要旋转的轴)
                    Matrix.scaleM(modelViewMatrix, 0, kBuildingScale,
                            kBuildingScale, kBuildingScale);
                }

                Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession
                        .getProjectionMatrix().getData(), 0, modelViewMatrix, 0);
                //两矩阵相乘，将结果置于modelViewProjection数组中
                //vuforiaAppSession.getProjectionMatrix().getData()相机位置矩阵
                // activate the shader program and bind the vertex/normal/tex coords
                GLES20.glUseProgram(shaderProgramID);
                SampleUtils.checkGLError("glUseProgram");//以上的GLES运行无错
                //楼房的渲染
                if (!mActivity.isExtendedTrackingActive()) {
                    GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                            false, 0, mCubeObject.getVertices());
                    //变量类型为vec4(x,y,z,1)，这里是3的缘故，
                    // 表示(x,y,z)后面那个比例系数1不用
                    SampleUtils.checkGLError("vert");
                    GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,
                            false, 0, mCubeObject.getNormals());
                    SampleUtils.checkGLError("normal");//这里出错gl_Normal代表顶点的法线
                    GLES20.glVertexAttribPointer(textureCoordHandle, 2,
                            GLES20.GL_FLOAT, false, 0, mCubeObject.getTexCoords());
                    SampleUtils.checkGLError("coord");
                    //启用或者禁用顶点属性数组，下面为启用
                    GLES20.glEnableVertexAttribArray(vertexHandle);
                    SampleUtils.checkGLError("vd");
                    GLES20.glEnableVertexAttribArray(normalHandle);
                    SampleUtils.checkGLError("nd");//这里出错
                    GLES20.glEnableVertexAttribArray(textureCoordHandle);
                    SampleUtils.checkGLError("td");

                    // activate texture 0, bind it, and pass to shader
                    //选择活动纹理单元。函数原型：
                    /*void glActiveTexture (int texture)
                    参数含义：
                    texture指定哪一个纹理单元被置为活动状态。texture必须是GL_TEXTUREi之一，*//*
                    其中0 <= i < GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS，初始值为GL_TEXTURE0。*/
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                    //确定了后续的纹理状态改变影响哪个纹理，纹理单元的数量是依据该纹理单元所被支持的具体实现。
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                            mTextures.get(textureIndex).mTextureID[0]);
                    GLES20.glUniform1i(texSampler2DHandle, 0);

                    // pass the model view matrix to the shader
                    GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                            modelViewProjection, 0);
                    GLES20.glEnable(GLES20.GL_ALPHA);
                    // finally draw the teapot
                    GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                            mCubeObject.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT,
                            mCubeObject.getIndices());
                    //以上无标记处无错误
                    // disable the enabled arrays
                    GLES20.glDisableVertexAttribArray(vertexHandle);
                    GLES20.glDisableVertexAttribArray(normalHandle);
                    GLES20.glDisableVertexAttribArray(textureCoordHandle);
                    SampleUtils.checkGLError("GOD7");
                } else {
                    //绘制大楼
                    GLES20.glDisable(GLES20.GL_CULL_FACE);
                    GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                            false, 0, mBuildingsModel.getVertices());
                    GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,
                            false, 0, mBuildingsModel.getNormals());
                    GLES20.glVertexAttribPointer(textureCoordHandle, 2,
                            GLES20.GL_FLOAT, false, 0, mBuildingsModel.getTexCoords());
                    GLES20.glEnableVertexAttribArray(vertexHandle);
                    GLES20.glEnableVertexAttribArray(normalHandle);
                    GLES20.glEnableVertexAttribArray(textureCoordHandle);
                    SampleUtils.checkGLError("GOD5");
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                            mTextures.get(3).mTextureID[0]);
                    GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                            modelViewProjection, 0);
                    GLES20.glUniform1i(texSampler2DHandle, 0);
                    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0,
                            mBuildingsModel.getNumObjectVertex());
                    SampleUtils.checkGLError("GOD6");
                    SampleUtils.checkGLError("Renderer DrawBuildings");
                }

                SampleUtils.checkGLError("Render Frame");
                //调试这里一直报错
                //GL_INVALID_VALUE, 0x0501
               /* Given when a value parameter is not a legal value for that function.
                This is only given for local problems;
                if the spec allows the value in certain circumstances,
                where other parameters or state dictate those circumstances,
                then GL_INVALID_OPERATION is the result instead.*/
                //https://www.opengl.org/wiki/OpenGL_Error

                GLES20.glDisable(GLES20.GL_DEPTH_TEST);
                drawTeapot(modelViewMatrix2, 1);


            }


        }//上述为1个for循环，会对所有Trackerable进行追踪
          /*  float[] modelViewMatrix = {
                    1.0f, 0, 0, 0,
                    0, -1.0f, 0, 0,
                    0, 0, -1.0f, 0,
                    120f, -50.0f, 600.0f, 1
            };
            float[] modelViewMatrix2 = {
                    1.0f, 0, 0, 0,
                    0, -1.0f, 0, 0,
                    0, 0, -1.0f, 0,
                    40f, -50.0f, 600.0f, 1
            };
            drawTeapot(modelViewMatrix,2);
            drawTeapot( modelViewMatrix2,4);*/
        mRenderer.end();
    }


    private void printUserData(Trackable trackable)
    {
        String userData = (String) trackable.getUserData();
        //下面也将Log.d改为Log.i
        Log.i(LOGTAG, "UserData:Retreived User Data	\"" + userData + "\"");
    }


    public void setTextures(Vector<Texture> textures)
    {
        mTextures = textures;

    }


    public void drawTeapot(float[] modelViewMatrix,int textureIndex){

     /*   TrackableResult result = state.getTrackableResult(tIdx);
        Trackable trackable = result.getTrackable();
        // trackable为每个可跟踪的内容，具有名字，id和类型
        printUserData(trackable);
        Matrix44F modelViewMatrix_Vuforia = Tool
                .convertPose2GLMatrix(result.getPose());
        // 接下来就获取姿态矩阵等，为一个4*4的矩阵，有四个表示坐标的行向量
        //姿态矩阵又称方向余弦矩阵
        //http://wenku.baidu.com/link?url=exe-qKW5y5vlczYRrhfdYm30S1batlszVGf1gwTtcUHUih-drophAUDnCAc4AM-BMIOPTtjfj_tcrxPRNkbsgvgLVAuetH2nMU2SWkHmh1_
        float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();*/
        Log.i(LOGTAG,"draw teapot");
        for (int i = 0; i < 16; i += 4) {

            Log.i(LOGTAG, modelViewMatrix[i] + "   " + modelViewMatrix[i + 1] + "   "
                    + modelViewMatrix[i + 2] + "   " + modelViewMatrix[i + 3] + "   ");
        }

        //int textureIndex = 3;
       /* int textureIndex = trackable.getName().equalsIgnoreCase("stones") ? 3
                : 2;
        textureIndex = trackable.getName().equalsIgnoreCase("tarmac") ? 1
                : textureIndex;*/
        //tracker包含chip，如果不是上述二者则为chip
        // deal with the modelview and projection matrices
        float[] modelViewProjection = new float[16];

        //以下关于矩阵的变换函数的部分介绍
        // http://blog.sina.com.cn/s/blog_a23d30f101018gl4.html
        if (!mActivity.isExtendedTrackingActive()) {
            //对茶壶数据矩阵做变换
          /*  if (TeapotAppear==false&&AnimationTZ >= 200.0f) {
                Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f,
                        AnimationTZ);
                AnimationTZ -= 20.0f;
                Matrix.rotateM(modelViewMatrix, 0, AnimationRZ, 0, 1.0f, 0);
                AnimationRZ += 10.0f;
            } else {
                    Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f,
                            200f);
              //  Matrix.rotateM(modelViewMatrix, 0, 0, 1.0f,0, 0);
            }
            //物体平移Matrix.translateM(mMMatrix,0,//偏移量,x, y, z//平移量)
            //将物体沿着Z轴上升
            //*/
          /*  Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f,
                    200f);*/
            Matrix.scaleM(modelViewMatrix, 0, 3.0f,
                    3.0f, 3.0f);
            //Matrix.scaleM(mMMatrix,sx,sy, sz//缩放因子)
            //http://www.360doc.com/content/14/1028/09/19175681_420513219.shtml相机的学习
        } else {
            //l楼房矩阵变换
            // Matrix.rotateM(modelViewMatrix, 0, 90.0f, 1.0f, 0, 0);
            //Matrix.rotateM(mMMatrix,0,//偏移量angle,//旋转角度x, y, z//需要旋转的轴)
            Matrix.scaleM(modelViewMatrix, 0, kBuildingScale,
                    kBuildingScale, kBuildingScale);
        }

        Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession
                .getProjectionMatrix().getData(), 0, modelViewMatrix, 0);
        //两矩阵相乘，将结果置于modelViewProjection数组中
        //vuforiaAppSession.getProjectionMatrix().getData()相机位置矩阵
        // activate the shader program and bind the vertex/normal/tex coords
        GLES20.glUseProgram(shaderProgramID);
        SampleUtils.checkGLError("glUseProgram");//以上的GLES运行无错
        //楼房的渲染
        // if (!mActivity.isExtendedTrackingActive()) {
        GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                false, 0, mTeapot.getVertices());
        //变量类型为vec4(x,y,z,1)，这里是3的缘故，
        // 表示(x,y,z)后面那个比例系数1不用
        SampleUtils.checkGLError("vert");
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,
                false, 0, mTeapot.getNormals());
        SampleUtils.checkGLError("normal");//这里出错gl_Normal代表顶点的法线
        GLES20.glVertexAttribPointer(textureCoordHandle, 2,
                GLES20.GL_FLOAT, false, 0, mTeapot.getTexCoords());
        SampleUtils.checkGLError("coord");
        //启用或者禁用顶点属性数组，下面为启用
        GLES20.glEnableVertexAttribArray(vertexHandle);
        SampleUtils.checkGLError("vd");
        GLES20.glEnableVertexAttribArray(normalHandle);
        SampleUtils.checkGLError("nd");//这里出错
        GLES20.glEnableVertexAttribArray(textureCoordHandle);
        SampleUtils.checkGLError("td");

        // activate texture 0, bind it, and pass to shader
        //选择活动纹理单元。函数原型：
                    /*void glActiveTexture (int texture)
                    参数含义：
                    texture指定哪一个纹理单元被置为活动状态。texture必须是GL_TEXTUREi之一，*//*
                    其中0 <= i < GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS，初始值为GL_TEXTURE0。*/
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //确定了后续的纹理状态改变影响哪个纹理，纹理单元的数量是依据该纹理单元所被支持的具体实现。
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                mTextures.get(textureIndex).mTextureID[0]);
        GLES20.glUniform1i(texSampler2DHandle, 0);

        // pass the model view matrix to the shader
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                modelViewProjection, 0);
        // finally draw the teapot
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                mTeapot.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT,
                mTeapot.getIndices());
        //以上无标记处无错误
        // disable the enabled arrays
        GLES20.glDisableVertexAttribArray(vertexHandle);
        GLES20.glDisableVertexAttribArray(normalHandle);
        GLES20.glDisableVertexAttribArray(textureCoordHandle);
        SampleUtils.checkGLError("GOD7");
        //   } else {


        SampleUtils.checkGLError("Render Frame");
        //调试这里一直报错
        //GL_INVALID_VALUE, 0x0501
               /* Given when a value parameter is not a legal value for that function.
                This is only given for local problems;
                if the spec allows the value in certain circumstances,
                where other parameters or state dictate those circumstances,
                then GL_INVALID_OPERATION is the result instead.*/
        //https://www.opengl.org/wiki/OpenGL_Error

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

    }

    public void initRenderingTeapot(){}
}
