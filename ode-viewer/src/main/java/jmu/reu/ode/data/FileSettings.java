package jmu.reu.ode.data;

/**
 * A class that defines the settings pertaining to how to use a file.
 * 
 * @author Benjamin Huber
 * @version 8/17/2022
 */
public class FileSettings {
    private String fileName;
    private String divider;
    private int fileSkip;

    public FileSettings(String fileName) {
        this.fileName = fileName;
        this.divider = " +";
        this.fileSkip = 1;
    }

    /**
     * Accessor method for fileName
     * 
     * @return fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Mutator method for fileName
     * 
     * @param fileName new FileName value
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Accessor method for divider.
     * 
     * @return divider
     */
    public String getDivider() {
        return divider;
    }

    /**
     * Mutator method for divider.
     * 
     * @param dividern new divider attribute
     */
    public void setDivider(String divider) {
        this.divider = divider;
    }

    /**
     * Accessor method for fileSkip.
     * 
     * @return fileSkip attribute
     */
    public int getFileSkip() {
        return fileSkip;
    }

    /**
     * Mutator method for fileSkip.
     * 
     * @param fileSkip new fileSkip value
     */
    public void setFileSkip(int fileSkip) {
        this.fileSkip = fileSkip;
    }
}
