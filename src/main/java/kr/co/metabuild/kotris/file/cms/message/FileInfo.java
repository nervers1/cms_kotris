package kr.co.metabuild.kotris.file.cms.message;

import java.util.Objects;

/**
 * 파일 정보
 */
public class FileInfo {
    // 파일명
    String name;
    // 파일사이즈
    long size;


    /**
     * Getter & Setter
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }


    /**
     * Equals & HashCode
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FileInfo)) return false;
        FileInfo fileInfo = (FileInfo) o;
        return getSize() == fileInfo.getSize() && Objects.equals(getName(), fileInfo.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getSize());
    }


    /**
     * toString
     */
    @Override
    public String toString() {
        return "FileInfo{" +
                "name='" + name + '\'' +
                ", size=" + size +
                '}';
    }
}
