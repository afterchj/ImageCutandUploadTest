package com.alanjet.imagecutanduploadtest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alanjet.imagecutanduploadtest.util.Constant;
import com.alanjet.imagecutanduploadtest.util.DynamicPermissionCheck;
import com.alanjet.imagecutanduploadtest.util.FileUtils;
import com.alanjet.imagecutanduploadtest.util.MyLog;
import com.alanjet.imagecutanduploadtest.util.RetrofitUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {
    //    private File file;
    String path = Environment.getExternalStorageDirectory() + "/aaa/bbb/";
    private String fileName = "9de2725281b44136b04e474d85061151.jpg";
    private MyImageView mImage;
    private Bitmap mBitmap;
    protected static final int CHOOSE_PICTURE = 0;
    protected static final int TAKE_PICTURE = 1;
    protected static Uri tempUri;
    private static final int CROP_SMALL_PICTURE = 2;

    private ProgressBar mPb;
    private TextView mTv;
    private TextView button;
    private Button video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        File f = new File(Environment.getExternalStorageDirectory(), "temp_image.jpg");
        System.out.println("----------------->" + f.getAbsolutePath());
        setContentView(R.layout.activity_main);
        initUI();
        initListeners();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload();
            }
        });

        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, VideoActivity.class);
                startActivity(intent);
            }
        });

    }

    private void initUI() {
        button = (TextView) findViewById(R.id.button);
        mPb = (ProgressBar) findViewById(R.id.pb);
        mTv = (TextView) findViewById(R.id.tv);
        mImage = (MyImageView) findViewById(R.id.iv_image);
        video = (Button) findViewById(R.id.video);
        Bitmap bitmap = BitmapFactory.decodeFile(path + fileName);
        if (bitmap != null) {
            mImage.setImageBitmap(bitmap);
        } else {
            mImage.setImageURL(Constant.HEAD.getBaseUrl());
        }
    }

    private void initListeners() {
        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DynamicPermissionCheck(MainActivity.this).checkPermission();
                showChoosePicDialog();
            }
        });
    }

    private void startDownload() {

//        final Bitmap[] bitmap = new Bitmap[1];
        String downloadUrl = "user/app-debug.apk";

        Call<ResponseBody> responseBodyCall = new RetrofitUtil(Constant.DEFAULT.getBaseUrl()).create(PostRequest_Interface.class).downloadFile(downloadUrl);

        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {

                MyLog.d("vivi", response.message() + "  length  " + response.body().contentLength() + "  type " + response.body().contentType());
//                InputStream is = response.body().byteStream();
//                bitmap[0] = BitmapFactory.decodeStream(is);
                //建立一个文件
                final File file = FileUtils.createFile(MainActivity.this);
                if (file.length() > 0) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                    startActivity(intent);
                    return;
                }
                //下载文件放在子线程
                new Thread() {
                    @Override
                    public void run() {
                        //保存到本地
                        FileUtils.writeFile2Disk(response, file, new HttpCallBack() {
                            @Override
                            public void onLoading(final long current, final long total) {
                                /**
                                 * 更新进度条
                                 */
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
//                                        MyLog.d("vivi", current + " to " + total);
//                                        MyLog.d("vivi", " runOnUiThread  " + currentThread().getName());
                                        mTv.setText(current + "");
                                        mPb.setMax((int) total);
                                        mPb.setProgress((int) current);
                                        if (current == total) {
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                                            startActivity(intent);
                                        }
                                    }
                                });
                            }
                        });
                    }
                }.start();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                MyLog.d("vivi", t.getMessage() + "  " + t.toString());
            }
        });

    }

    /**
     * 显示修改图片的对话框
     */
    protected void showChoosePicDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("添加图片");
        String[] items = {"选择本地照片", "拍照"};
        builder.setNegativeButton("取消", null);
        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case CHOOSE_PICTURE: // 选择本地照片
                        Intent intent = new Intent(Intent.ACTION_PICK);//返回被选中项的URI
                        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");//得到所有图片的URI
                        startActivityForResult(intent, CHOOSE_PICTURE);
                        break;
                    case TAKE_PICTURE: // 拍照
                        Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        tempUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "temp_image.jpg"));
                        // 将拍照所得的相片保存到SD卡根目录
                        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
                        startActivityForResult(openCameraIntent, TAKE_PICTURE);
                        break;
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == MainActivity.RESULT_OK) {
            switch (requestCode) {
                case TAKE_PICTURE:
                    cutImage(tempUri); // 对图片进行裁剪处理
                    break;
                case CHOOSE_PICTURE:
                    cutImage(data.getData()); // 对图片进行裁剪处理
                    break;
                case CROP_SMALL_PICTURE:
                    if (data != null) {
                        try {
                            setImageToView(data); // 让刚才选择裁剪得到的图片显示在界面上
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }

    /**
     * 裁剪图片方法实现
     */
    protected void cutImage(Uri uri) {
        if (uri == null) {
            Log.i("tip", "The uri is not exist.");
        }
        tempUri = uri;
        Intent intent = new Intent("com.android.camera.action.CROP");
        //com.android.camera.action.CROP这个action是用来裁剪图片用的
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_SMALL_PICTURE);
    }

    /**
     * 保存裁剪之后的图片数据
     */
    protected void setImageToView(Intent data) throws IOException {
        Bundle extras = data.getExtras();
        if (extras != null) {
            mBitmap = extras.getParcelable("data");
            //这里图片是方形的，可以用一个工具类处理成圆形（很多头像都是圆形，这种工具类网上很多不再详述）
            mImage.setImageBitmap(mBitmap);//显示图片
        }

        File file = saveFile(mBitmap);
        upload(file);

    }

    public File saveFile(Bitmap bitmap) {
        File myCaptureFile = new File(path + fileName);
        File dirFile = new File(path);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        BufferedOutputStream bos;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return myCaptureFile;
    }

    public void upload(File file) {
        //创建Retrofit对象
        //创建 网络请求接口 的实例
        String descriptionString = "This is a params";
        RequestBody uid = RequestBody.create(MediaType.parse("text/plain"), "14715689");
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        Call<Translation> call = new RetrofitUtil(Constant.UMS3_CLIENT2.getBaseUrl()).create(PostRequest_Interface.class).upload(uid, body);

        call.enqueue(new Callback<Translation>() {
            @Override
            public void onResponse(Call<Translation> call, Response<Translation> response) {
                System.out.println("result=" + response.body().toString());
                if (response.body() != null) {
                    try {
                        Toast.makeText(MainActivity.this, "头像上传成功！", Toast.LENGTH_SHORT).show();
                        System.out.println("result=" + response.body().getResult() + ",headUrl=" + response.body().getHeadUrl());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Translation> call, Throwable t) {
                Toast.makeText(MainActivity.this, "头像上传失败！", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            Bundle data = msg.getData();
//            String val = data.getString("value");
            if (msg.what == 000) {
                Toast.makeText(MainActivity.this, "头像上传成功！", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "头像上传失败！", Toast.LENGTH_SHORT).show();
            }
            Log.i("result", "请求结果:" + msg.what);
        }
    };

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // TODO: http request.
            String param = "{\"task_id\":\"29630\",\"auth_id\":\"1000375122\"}";
            File file = saveFile(mBitmap);
            Map<String, String> params = new HashMap<String, String>();
            params.put("params", fileName.substring(fileName.lastIndexOf(".")));
            params.put("desc", "测试内容");
            Map<String, File> files = new HashMap<String, File>();
            files.put("file", file);
            String response = "";
            try {
//                response = UploadUtil.uploadFile(uploadUrl, params, file);
                response = HttpClientUtil.uploadFile(Constant.UMS3_CLIENT2.getBaseUrl() + "upload", param, file);
            } catch (IOException e) {
                e.printStackTrace();
//                handler.sendEmptyMessage(200);
            }
            System.out.println("response=" + response);
//            Message msg = new Message();
//            msg.what = Integer.valueOf(response);
//            Bundle data = new Bundle();
//            data.putString("result", response);
//            msg.setData(data);
//            handler.sendEmptyMessage(Integer.valueOf(response));
        }
    };
    //为了下载图片资源，开辟一个新的子线程
    Thread downloadThread = new Thread() {
        public void run() {
            //下载图片的路径
            String iPath = "http://192.168.51.75:8080/ums3-client2/img/" + fileName;
            try {
                //对资源链接
                URL url = new URL(iPath);
                //打开输入流
//                InputStream inputStream = url.openStream();
                //对网上资源进行下载转换位图图片
//                bitmap = BitmapFactory.decodeStream(inputStream);
//                handler.sendEmptyMessage(111);
//                inputStream.close();

                //再一次打开
                InputStream inputStream = url.openStream();
                File file = new File(Environment.getExternalStorageDirectory() + File.separator + fileName);
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                int hasRead = 0;
                while ((hasRead = inputStream.read()) != -1) {
                    fileOutputStream.write(hasRead);
                }
                fileOutputStream.close();
                inputStream.close();
//                handler.sendEmptyMessage(000);
            } catch (MalformedURLException e) {
                e.printStackTrace();
//                handler.sendEmptyMessage(200);
            } catch (IOException e) {
                e.printStackTrace();
//                handler.sendEmptyMessage(200);
            }
        }
    };
}
