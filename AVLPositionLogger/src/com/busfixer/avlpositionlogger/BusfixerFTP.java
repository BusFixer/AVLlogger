package com.busfixer.avlpositionlogger;

/*
* This class is using org.apache.commons-net-3.3.jar
* 
*  and some snippets that I picked up on the Internet
*   plus some of my own stuff
*  this license may be required. here it is just in case.
*  
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import android.util.Log;

public class BusfixerFTP {

	public static final String TAG = "BF_ftp";
	public FTPClient mFTPClient = null;

	// Constructor
	BusfixerFTP() {
	}

	// Method to connect to FTP server:

	public boolean ftpConnect(String host, String username, String password,
			int port) {
		try {
			mFTPClient = new FTPClient();
			// connecting to the host
			mFTPClient.connect(host, port);

			// now check the reply code, if positive mean connection success
			if (FTPReply.isPositiveCompletion(mFTPClient.getReplyCode())) {
				// login using username & password
				boolean status = mFTPClient.login(username, password);

				/*
				 * Set File Transfer Mode
				 * 
				 * To avoid corruption issue you must specified a correct
				 * transfer mode, such as ASCII_FILE_TYPE, BINARY_FILE_TYPE,
				 * EBCDIC_FILE_TYPE .etc. Here, I use BINARY_FILE_TYPE for
				 * transferring text, image, and compressed files.
				 */
				mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
				mFTPClient.enterLocalPassiveMode();

				return status;
			}
		} catch (Exception e) {
			Log.d(TAG, "Error: could not connect to host " + host);
		}

		return false;
	}

	// Method to disconnect from FTP server:

	public boolean ftpDisconnect() {
		try {
			mFTPClient.logout();
			mFTPClient.disconnect();
			return true;
		} catch (Exception e) {
			Log.d(TAG, "Error occurred while disconnecting from ftp server.");
		}

		return false;
	}

	// Method to get current working directory:

	public String ftpGetCurrentWorkingDirectory() {
		try {
			String workingDir = mFTPClient.printWorkingDirectory();
			return workingDir;
		} catch (Exception e) {
			Log.d(TAG, "Error: could not get current working directory.");
		}

		return null;
	}

	// Method to change working directory: // returns false if current

	public boolean ftpChangeDirectory(String directory_path) {
		try {
			return mFTPClient.changeWorkingDirectory(directory_path);
		} catch (Exception e) {
			Log.d(TAG, "Error: could not change directory to " + directory_path);
		}

		return false;
	}

	// method to fetch FTP Reply String

	public String getReplyString() {
		String reply = mFTPClient.getReplyString();
		return reply;
	}

	// Method to check file exist in current directory

	public boolean checkFileExists(String filename) {
		boolean ret = false;
		FTPFile[] file;
		String fname = "";
		try {
			file = mFTPClient.listFiles(filename);

			if (file.length != 0) {
				fname = file[0].getName();
				ret = filename.equals(fname);
			}
		} catch (IOException e) {
			ret = false;
		}
		return ret;
	}

	// Method to check file exist in named directory

	public boolean checkFileExists(String filename, String DirName) {
		boolean ret = false;
		FTPFile[] file = null;
		String fname = "";

		try {
			if (ftpChangeDirectory(DirName)) {
				file = mFTPClient.listFiles(filename);
			}
			if (file.length != 0) {
				fname = file[0].getName();
				ret = filename.equals(fname);
			}
		} catch (IOException e) {
			ret = false;
		}
		return ret;
	}

	// Method to list all files in a directory: to Log

	public void ftpPrintFilesList(String dir_path) {
		try {
			FTPFile[] ftpFiles = mFTPClient.listFiles(dir_path);
			int length = ftpFiles.length;

			for (int i = 0; i < length; i++) {
				String name = ftpFiles[i].getName();
				boolean isFile = ftpFiles[i].isFile();

				if (isFile) {
					Log.i(TAG, "File : " + name);
				} else {
					Log.i(TAG, "Directory : " + name);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Method to return String[] list of all files in current directory:

	public String[] ftpFilesList() {
		String list[] = null;
		try {
			list = mFTPClient.listNames();
		} catch (IOException e) {
		}
		return list;
	}

	// Method to return String[] list of all files in a directory:

	public String[] ftpFilesList(String dir_path) {
		String list[] = null;
		try {
			list = mFTPClient.listNames(dir_path);
		} catch (IOException e) {
			// String reply =mFTPClient.getReplyString();
		}
		return list;
	}

	// Method to return CSV list all files in a directory:

	public String ftpFilesListCSV(String dir_path) {
		String filenames = "";
		//
		try {

			FTPFile[] ftpFiles = mFTPClient.listFiles(dir_path);
			int length = ftpFiles.length;

			for (int i = 0; i < length; i++) {

				boolean isFile = ftpFiles[i].isFile();

				if (isFile) {
					filenames = filenames + ftpFiles[i].getName() + ",";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return filenames;
	}

	// Method to find name of last file listed in a directory:
	// We know it contains only files

	public String LastFileName(String dir_path) {
		String filename = "";

		try {
			FTPFile[] ftpFiles = mFTPClient.listFiles(dir_path);
			int length = ftpFiles.length;

			filename = ftpFiles[length - 1].getName();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return filename;
	}

	// Method to create new directory:

	public boolean ftpMakeDirectory(String new_dir_path) {
		try {
			boolean status = mFTPClient.makeDirectory(new_dir_path);
			return status;
		} catch (Exception e) {
			Log.d(TAG, "Error: could not create new directory named "
					+ new_dir_path);
		}

		return false;
	}

	// Method to delete/remove a directory:

	public boolean ftpRemoveDirectory(String dir_path) {
		try {
			boolean status = mFTPClient.removeDirectory(dir_path);
			return status;
		} catch (Exception e) {
			Log.d(TAG, "Error: could not remove directory named " + dir_path);
		}

		return false;
	}

	// Method to delete a file:

	public boolean ftpRemoveFile(String filePath) {
		try {
			boolean status = mFTPClient.deleteFile(filePath);
			return status;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	// Method to rename a file:

	public boolean ftpRenameFile(String from, String to) {
		try {
			boolean status = mFTPClient.rename(from, to);
			return status;
		} catch (Exception e) {
			Log.d(TAG, "Could not rename file: " + from + " to: " + to);
		}

		return false;
	}

	// Method to download a file from FTP server:

	/**
	 * mFTPClient: FTP client connection object (see FTP connection example)
	 * srcFilePath: path to the source file in FTP server desFilePath: path to
	 * the destination file to be saved in sdcard
	 */
	public boolean ftpDownload(String srcFilePath, String desFilePath) {
		boolean status = false;
		try {
			FileOutputStream desFileStream = new FileOutputStream(desFilePath);
			;
			status = mFTPClient.retrieveFile(srcFilePath, desFileStream);
			desFileStream.close();

			return status;
		} catch (Exception e) {
			Log.d(TAG, "download failed");
		}

		return status;
	}

	// Method to upload a file to FTP server:

	/**
	 * mFTPClient: FTP client connection object (see FTP connection example)
	 * srcFilePath: source file path in sdcard desFileName: file name to be
	 * stored in FTP server desDirectory: directory path where the file should
	 * be upload to
	 */
	public boolean ftpUpload(String srcFilePath, String desFileName,
			String desDirectory) {
		boolean status = false;
		try {
			FileInputStream srcFileStream = new FileInputStream(srcFilePath);

			// change working directory to the destination directory
			if (ftpChangeDirectory(desDirectory)) {
				status = mFTPClient.storeFile(desFileName, srcFileStream);
			}

			srcFileStream.close();
			return status;
		} catch (Exception e) {
			Log.d(TAG, "upload failed");
		}

		return status;
	}

	/**
	 * mFTPClient: FTP client connection object (see FTP connection example)
	 * srcFilePath: source file path in sdcard desFileName: file name to be
	 * stored in FTP server current directory
	 */
	public boolean ftpUpload(String srcFilePath, String desFileName) {
		boolean status = false;
		try {
			FileInputStream srcFileStream = new FileInputStream(srcFilePath);

			status = mFTPClient.storeFile(desFileName, srcFileStream);

			srcFileStream.close();
			return status;
		} catch (Exception e) {
			Log.d(TAG, "upload failed");
		}

		return status;
	}

	/**
	 * mFTPClient: FTP client connection object (see FTP connection example)
	 * srcFilePath: source file path on local device desFileName: file name to
	 * be stored in FTP server desDirectory: directory path where the file
	 * should be upload to
	 */

	public boolean ftpAppendFile(String srcFilePath, String desFileName,
			String desDirectory) {
		boolean status = false;
		try {
			FileInputStream srcFileStream = new FileInputStream(srcFilePath);

			// change working directory to the destination directory
			if (ftpChangeDirectory(desDirectory)) {
				status = mFTPClient.appendFile(desFileName, srcFileStream);
			}

			srcFileStream.close();
			return status;
		} catch (Exception e) {
			Log.d(TAG, "append failed");
		}

		return status;
	}

	// Appends to a file on the server with the given name, taking input from
	// the given InputStream
	/**
	 * remoteFileName is the name of file on server to append file to append
	 * needs to be in current directory
	 * 
	 * Create a InputStream from text this way:
	 * 
	 * String inputText = "Text to be appended"; InputStream inStream = null;
	 * try { inStream = new ByteArrayInputStream(inputText.getBytes("UTF-8")); }
	 * catch (UnsupportedEncodingException e) { }
	 */

	public boolean ftpappendFile(String remoteFileName, InputStream local) {
		boolean status = false;
		try {
			status = mFTPClient.appendFile(remoteFileName, local);
			local.close();
			return status;
		} catch (Exception e) {
			Log.d(TAG, "append failed");
		}

		return status;
	}

	// method to append text to file on server in current directory

	public boolean AppendTextToFtpFile(String remoteFileName,
			String TextToAppend) {
		boolean status = false;
		String inputText = TextToAppend;
		InputStream inStream = null;
		try {
			inStream = new ByteArrayInputStream(inputText.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
		}

		try {
			status = mFTPClient.appendFile(remoteFileName, inStream);
			inStream.close();
			return status;
		} catch (Exception e) {
			Log.d(TAG, "append failed");
		}

		return status;
	}

	// method to append text to file on server in supplied directory
// not tested
	public boolean AppendTextToFtpFile(String remoteFileName,
			String TextToAppend, String DirName) {
		boolean status = false;
		String inputText = TextToAppend;
		InputStream inStream = null;
		try {
			inStream = new ByteArrayInputStream(inputText.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
		}

		try {
			if (ftpChangeDirectory(DirName)) {
				status = mFTPClient.appendFile(remoteFileName, inStream);
			}
			inStream.close();
			return status;
		} catch (Exception e) {
			Log.d(TAG, "append failed");
		}

		return status;
	}

}
