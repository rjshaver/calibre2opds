package com.gmail.dpierron.calibre.opds;
/**
 *
 */
import com.gmail.dpierron.calibre.cache.CachedFile;
import com.gmail.dpierron.calibre.cache.CachedFileManager;
import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.thumbnails.CreateThumbnail;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


public abstract class ImageManager {
  private final static Logger logger = Logger.getLogger(ImageManager.class);

  private boolean imageSizeChanged=false;
  private Map<File, File> imagesToGenerate;
  private long timeInImages = 0;

  private int imageHeight = 1;

  abstract String getResultFilename(Book book);
  abstract String getImageHeightDat();

  public final static ImageManager newThumbnailManager() {
    return new ThumbnailManager(ConfigurationManager.INSTANCE.getCurrentProfile().getThumbnailHeight());
  }
  
  public final static ImageManager newCoverManager() {
    return new CoverManager(ConfigurationManager.INSTANCE.getCurrentProfile().getCoverHeight());
  }

  ImageManager(int imageHeight) {
    imagesToGenerate = new HashMap<File, File>();
    timeInImages = 0;
    this.imageHeight = imageHeight;


    File imageSizeFile = new File(ConfigurationManager.INSTANCE.getCurrentProfile().getDatabaseFolder(), getImageHeightDat());

    if (!imageSizeFile.exists())
      imageSizeChanged = true;
    else {
      try {
        ObjectInputStream ois = null;
        try {
          ois = new ObjectInputStream(new FileInputStream(imageSizeFile));
          int oldSize = ois.readInt();
          imageSizeChanged = oldSize != imageHeight;
        } finally {
          if (ois != null)
            ois.close();
        }
      } catch (Exception e) {
        // ITIMPI:  Why is this commented out?  Seems it should ne logged to me
        // logger.warn("cannot read the file " + imageSizeFile.getAbsolutePath());
        imageSizeChanged = true;
      }
    }
  }

  public void setImageToGenerate(File reducedCoverFile, File coverFile) {
    if (!imagesToGenerate.containsKey(reducedCoverFile)) {
      imagesToGenerate.put(reducedCoverFile, coverFile);
    }
  }


  boolean hasImageSizeChanged() {
    return imageSizeChanged;
  }

  String getImageUri(Book book) {
    return FeedHelper.INSTANCE.urlEncode("../../" + book.getPath() + "/" + getResultFilename(book), true);
  }

  public void writeImageHeightFile() {
	File imageSizeFile = new File(ConfigurationManager.INSTANCE.getCurrentProfile().getDatabaseFolder(), getImageHeightDat());
    try
    {
      ObjectOutputStream oos = null;
      try
      {
        oos = new ObjectOutputStream(new FileOutputStream(imageSizeFile));
        oos.writeInt(imageHeight);
      } finally {
        if (oos != null)
          oos.close();
      }
    } catch (Exception e) {
        // ITIMPI:   Why is this commented out - seems it should not be to me?
        // logger.warn("cannot write the file " + imageSizeFile.getAbsolutePath());
    }
  }

  public long generateImages()
  {
    long countFiles = 0;
    for (Map.Entry<File, File> fileEntry : imagesToGenerate.entrySet()) {
      File imageFile = fileEntry.getKey();
      if (logger.isDebugEnabled())
        logger.debug("generateImages: "+imageFile.getAbsolutePath());
      File coverFile = fileEntry.getValue();
      CatalogContext.INSTANCE.getCallback().incStepProgressIndicatorPosition();
      CatalogContext.INSTANCE.getCallback().showMessage(imageFile.getParentFile().getName() + File.separator + imageFile.getName());
      long now = System.currentTimeMillis();
      try {
        CreateThumbnail ct = new CreateThumbnail(coverFile.getAbsolutePath());
        ct.getThumbnail(imageHeight, CreateThumbnail.VERTICAL);
        ct.saveThumbnail(imageFile, CreateThumbnail.IMAGE_JPEG);
        // bug #732821 Ensure file added to those cached for copying
        CachedFile cf = CachedFileManager.INSTANCE.addCachedFile(imageFile);
        if (logger.isTraceEnabled())
              logger.trace ("generateImages: added new thumbnail file " + imageFile.getAbsolutePath() + " to list of files to copy");
        countFiles++;         // Update count of files processed
      } catch (Exception e) {
        CatalogContext.INSTANCE.getCallback().errorOccured(Localization.Main.getText("error.generatingThumbnail", coverFile.getAbsolutePath()),e);
      }
      timeInImages += (System.currentTimeMillis() - now);
    }
    writeImageHeightFile();
    return countFiles;
  }

  public int getNbImagesToGenerate() {
    return imagesToGenerate.size();
  }

}
