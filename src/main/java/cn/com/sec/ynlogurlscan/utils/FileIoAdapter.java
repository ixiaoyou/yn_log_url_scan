package cn.com.sec.ynlogurlscan.utils;


import com.hankcs.hanlp.corpus.io.IIOAdapter;
import com.hankcs.hanlp.corpus.io.IOUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class FileIoAdapter implements IIOAdapter {

    @Override
    public InputStream open(String path) throws IOException
    {
        System.out.println("path==========="+path);
        InputStream in = FileIoAdapter.class.getClassLoader().getResourceAsStream(path);
        return FileIoAdapter.class.getClassLoader().getResourceAsStream(path);
    }

    @Override
    public OutputStream create(String path) throws IOException
    {
        if (IOUtil.isResource(path)) throw new IllegalArgumentException("不支持写入jar包资源路径" + path);
        return new FileOutputStream(path);
    }

}
