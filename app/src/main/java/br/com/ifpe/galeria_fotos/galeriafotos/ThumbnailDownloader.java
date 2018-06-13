package br.com.ifpe.galeria_fotos.galeriafotos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Adrielly, Kaio e Marcela on 19/05/2018.
 */

public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MENSAGEM_DOWNLOAD = 0;

    private boolean mHasQuit = false;
    private Handler mRequestHandler;
    private Handler mResponseHandler;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;

    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {
        mThumbnailDownloadListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MENSAGEM_DOWNLOAD) {
                    T target = (T) msg.obj;
                    Log.i(TAG, "Recebido um pedido de URL: " + mRequestMap.get(target));
                    handleRequest(target);
                }
            }
        };
    }

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    public void queueThumbnail(T target, String url) {
        Log.i(TAG, "Recebendo uma URL: " + url);

        if(url == null) {
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MENSAGEM_DOWNLOAD, target).sendToTarget();
        }
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MENSAGEM_DOWNLOAD);
        mRequestMap.clear();
    }

    private void handleRequest(final T target) {
        try {
            final String url = mRequestMap.get(target);

            if(url == null) {
                return;
            }

            byte[] bytes = new FlickrFetchr().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            Log.i(TAG, "Bitmap created");

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRequestMap.get(target) != url || mHasQuit) {
                        return;
                    }

                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
                }
            });

        } catch(IOException ex) {
            Log.e(TAG, "Erro no download da imagem. ", ex);
        }
    }

}
