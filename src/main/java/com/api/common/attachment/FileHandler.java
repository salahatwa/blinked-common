package com.api.common.attachment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

import javax.imageio.ImageReader;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import com.api.common.utils.ImageUtils;


/**
 * File handler interface.
 *
 * @author ssatwa
 * @date 2019-03-27
 */
public interface FileHandler {
	/**
	 * Path separator.
	 */
	public static final String FILE_SEPARATOR = File.separator;

	MediaType IMAGE_TYPE = MediaType.valueOf("image/*");

	/**
	 * Uploads file.
	 *
	 * @param file multipart file must not be null
	 * @return upload result
	 * @throws FileOperationException throws when fail to upload the file
	 */
	@NonNull
	UploadResult upload(@NonNull MultipartFile file);

	/**
	 * Check if the current file is an image.
	 *
	 * @param file multipart file must not be null
	 * @return true if the current file is an image, false otherwise
	 */
	default boolean isImageType(@NonNull MultipartFile file) {
		String mediaType = file.getContentType();
		return mediaType != null && IMAGE_TYPE.includes(MediaType.valueOf(mediaType));
	}

	/**
	 * @param uploadResult      updated result must not be null
	 * @param file              multipart file must not be null
	 * @param thumbnailSupplier thumbnail supplier
	 */
	default void handleImageMetadata(@NonNull MultipartFile file, @NonNull UploadResult uploadResult,
			@Nullable Supplier<String> thumbnailSupplier) {
		if (isImageType(file)) {
			// Handle image
			try (InputStream is = file.getInputStream()) {
				ImageReader image = ImageUtils.getImageReaderFromFile(is, uploadResult.getSuffix());
				uploadResult.setWidth(image.getWidth(0));
				uploadResult.setHeight(image.getHeight(0));
				if (thumbnailSupplier != null) {
					uploadResult.setThumbPath(thumbnailSupplier.get());
				}
			} catch (IOException | OutOfMemoryError e) {
				// ignore IOException and OOM
				LoggerFactory.getLogger(getClass()).warn("Failed to fetch image meta data", e);
			}
		}
		if (StringUtils.isBlank(uploadResult.getThumbPath())) {
			uploadResult.setThumbPath(uploadResult.getFilePath());
		}
	}

	/**
	 * Deletes file.
	 *
	 * @param key file key must not be null
	 * @throws FileOperationException throws when fail to delete the file
	 */
	void delete(@NonNull String key);

	/**
	 * Get attachment type is supported.
	 *
	 * @return attachment type
	 */
	AttachmentType getAttachmentType();

	/**
	 * Normalize directory full name, ensure the end path separator.
	 *
	 * @param dir directory full name must not be blank
	 * @return normalized directory full name with end path separator
	 */
	@NonNull
	static String normalizeDirectory(@NonNull String dir) {
		Assert.hasText(dir, "Directory full name must not be blank");

		return StringUtils.appendIfMissing(dir, FILE_SEPARATOR);
	}

}