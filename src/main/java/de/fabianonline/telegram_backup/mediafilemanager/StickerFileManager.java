/* Telegram_Backup
 * Copyright (C) 2016 Fabian Schlenz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package de.fabianonline.telegram_backup.mediafilemanager;

import de.fabianonline.telegram_backup.UserManager;
import de.fabianonline.telegram_backup.Database;
import de.fabianonline.telegram_backup.StickerConverter;
import de.fabianonline.telegram_backup.DownloadProgressInterface;
import de.fabianonline.telegram_backup.DownloadManager;
import de.fabianonline.telegram_backup.Config;

import com.github.badoualy.telegram.api.TelegramClient;
import com.github.badoualy.telegram.tl.core.TLIntVector;
import com.github.badoualy.telegram.tl.core.TLObject;
import com.github.badoualy.telegram.tl.api.messages.TLAbsMessages;
import com.github.badoualy.telegram.tl.api.messages.TLAbsDialogs;
import com.github.badoualy.telegram.tl.api.*;
import com.github.badoualy.telegram.tl.api.upload.TLFile;
import com.github.badoualy.telegram.tl.exception.RpcErrorException;
import com.github.badoualy.telegram.tl.api.request.TLRequestUploadGetFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.net.URL;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;

public class StickerFileManager extends DocumentFileManager {
	private static Logger logger = LoggerFactory.getLogger(StickerFileManager.class);
	
	public StickerFileManager(String prefix, TLMessage msg, UserManager user, TelegramClient client) {
		super(prefix, msg, user, client);
	}
	
	public boolean isSticker() { return true; }
	
	private String getFilenameBase() {
		TLDocumentAttributeSticker sticker = null;
		for(TLAbsDocumentAttribute attr : doc.getAttributes()) {
			if (attr instanceof TLDocumentAttributeSticker) {
				sticker = (TLDocumentAttributeSticker)attr;
			}
		}
		
		StringBuilder file = new StringBuilder();
		if (sticker.getStickerset() instanceof TLInputStickerSetShortName) {
			file.append(((TLInputStickerSetShortName)sticker.getStickerset()).getShortName());
		} else if (sticker.getStickerset() instanceof TLInputStickerSetID) {
			file.append(((TLInputStickerSetID)sticker.getStickerset()).getId());
		}
		file.append("_");
		file.append(sticker.getAlt().hashCode());
		return file.toString();
	}
	
	public String getTargetFilename() {
		return getFilenameBase() + "." + getExtension();
	}
	
	public String getTargetPath() {
		String path = user.getFileBase() + Config.FILE_FILES_BASE + File.separatorChar + Config.FILE_STICKER_BASE + File.separatorChar;
		new File(path).mkdirs();
		return path;
	}
	
	public void download() throws RpcErrorException, IOException {
		String old_file = Config.FILE_BASE + File.separatorChar + Config.FILE_STICKER_BASE + File.separatorChar + getTargetFilename();
		
		logger.trace("Old filename exists: {}", new File(old_file).exists());
		
		if (new File(old_file).exists()) {
			Files.copy(Paths.get(old_file), Paths.get(getTargetPathAndFilename()), StandardCopyOption.REPLACE_EXISTING);
			return;
		}
		super.download();
	}	
	
	public String getExtension() { return "webp"; }
	
	public String getLetter() { return "s"; }
	public String getName() { return "sticker"; }
	public String getDescription() { return "Sticker"; }
}
