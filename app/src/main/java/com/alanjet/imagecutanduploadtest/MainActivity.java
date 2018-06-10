package com.alanjet.imagecutanduploadtest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private File file;
    private String fileName = UUID.randomUUID().toString().replace("-", "") + ".jpg";
    private ImageView mImage;
    String uploadUrl = "http://192.168.0.6:8080/web-ssm/file/upload2";
    private Bitmap mBitmap;
    private Bitmap bitmap;
    protected static final int CHOOSE_PICTURE = 0;
    protected static final int TAKE_PICTURE = 1;
    protected static Uri tempUri;
    private static final int CROP_SMALL_PICTURE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        File f = new File(Environment.getExternalStorageDirectory(), "temp_image.jpg");
        System.out.println("----------------->" + f.getAbsolutePath());
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }

        final MyImageView myImageView = (MyImageView) findViewById(R.id.iv_image);
        myImageView.setImageURL("http://192.168.0.6:8080/web-ssm/img/head.jpg");

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(t).start();
                if (bitmap != null) {
                    mImage.setImageBitmap(bitmap);
                }
            }
        });
        initUI();
        initListeners();
    }

    private void initUI() {
        mImage = (ImageView) findViewById(R.id.iv_image);
    }

    private void initListeners() {
        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mImage.setImageURI(Uri.parse("http://localhost:8080/web-ssm/img/head.jpg"));
                showChoosePicDialog();
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
            Log.i("alanjet", "The uri is not exist.");
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
            file = saveFile(mBitmap, fileName);
            //这里图片是方形的，可以用一个工具类处理成圆形（很多头像都是圆形，这种工具类网上很多不再详述）
            mImage.setImageBitmap(mBitmap);//显示图片
            new Thread(runnable).start();

            //在这个地方可以写上上传该图片到服务器的代码，后期将单独写一篇这方面的博客，敬请期待...
        }
    }

    public File saveFile(Bitmap bm, String fileName) throws IOException {
        String path = Environment.getExternalStorageDirectory() + "/heads/";
        File dirFile = new File(path);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        File myCaptureFile = new File(path + fileName);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        bos.flush();
        bos.close();
        return myCaptureFile;
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
                Toast.makeText(MainActivity.this, "系统异常！", Toast.LENGTH_SHORT).show();
            }
            Log.i("result", "请求结果:" + msg.what);
        }
    };

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // TODO: http request.
//            File file = new File(Environment.getExternalStorageDirectory(), "temp_image.jpg");
            Map<String, String> params = new HashMap<String, String>();
            params.put("fileName", "测试文件");
            params.put("desc", "测试内容");
            Map<String, File> files = new HashMap<String, File>();
            files.put("file", file);
            String response = "";
            try {
                response = UploadUtil.uploadFile(uploadUrl, params, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("response=" + response);
            Message msg = new Message();
            msg.what = Integer.valueOf(response);
//            Bundle data = new Bundle();
//            data.putString("result", response);
//            msg.setData(data);
            handler.sendMessage(msg);
        }
    };
    //为了下载图片资源，开辟一个新的子线程
    Thread t = new Thread() {
        public void run() {
            //下载图片的路径
            String iPath = "http://192.168.0.6:8080/web-ssm/img/" + fileName;
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
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
}
