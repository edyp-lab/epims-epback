/*
 * Copyright (C) 2021
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program;
 * If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.edyp.epims.transfer.dataformat.nems;

import fr.edyp.epims.transfer.model.AbstractAnalysis;
import fr.edyp.epims.transfer.model.DataFormat;

import java.io.*;
import java.util.ArrayList;

public class NemsAnalysis extends AbstractAnalysis {

    private static final String ANALYSIS_FILE_EXT = "zip";
    private final ArrayList<NemsFactory.Mset> msetArrayList;
    private NemsFormat dataFormat;
    private File zipFile = null;
    private final String zipFileName;
    private ArrayList<File> allAcqFiles;

    private final NemsFilter nemsFilter = new NemsFilter();

    //TODO : Verify XML instrum cache
    public NemsAnalysis(File f, NemsFormat format, String sample, ArrayList<NemsFactory.Mset> msetArrayList) {
        analysisFile = f;
        status = ANALYSIS_STATUS_UNKNOWN;
        dataFormat = format;
        this.sample = sample;
        name = sample+"_"+f.getName();
        zipFileName = name+ANALYSIS_FILE_EXT;
        determineType();
        this.msetArrayList = msetArrayList;

        allAcqFiles = new ArrayList<>();
        listFiles(analysisFile, allAcqFiles, msetArrayList);

    }

    @Override
    public File getFileToTransfer() {
        if(zipFile == null) {
            try {
                if (!allAcqFiles.isEmpty()) {
                    zipFile = new File(analysisFile.getParentFile(), zipFileName);
                    //                zipFile = zipDir(name + ".zip", analysisFile, msetArrayList);
                    boolean zipCreateSucess = fr.edyp.epims.transfer.util.FileUtils.copyFilesToOneZip(zipFile, allAcqFiles, analysisFile.getParentFile(), true);
                    if (zipCreateSucess)
                        estimatedSize = zipFile.length();
                    else
                        zipFile = null;
                }
            } catch (Exception e) {
                return null;
            }
        }
        return zipFile;
    }

    @Override
    public boolean isTransferFileTempo() {
        return true;
    }


    private void listFiles(File dirObj, ArrayList<File> fileList, ArrayList<NemsFactory.Mset> msetArrayList) {
        File[] files = dirObj.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                String name = file.getName();

                if (name.equals("Unlocked")) {
                    listFiles(file, fileList, null);
                    continue;
                }

                if (msetArrayList != null) {
                    for (NemsFactory.Mset mset : msetArrayList) {
                        if (mset.msetFile.getName().equals(name)) {
                            listFiles(file, fileList, null);
                            break;
                        }
                    }
                } else {
                    listFiles(file, fileList, null);
                }

                continue;
            }

            fileList.add(file);

        }
    }

//    private File zipDir(String zipFileName, File dirObj, ArrayList<NemsFactory.Mset> msetArrayList) throws Exception {
//
//        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
//
//        addDirectoryToZip(dirObj, dirObj, out, msetArrayList);
//
//        out.close();
//        return new File(zipFileName);
//    }
//
//    private void addDirectoryToZip(File parentDir, File dirObj, ZipOutputStream out, ArrayList<NemsFactory.Mset> msetArrayList) throws IOException {
//        File[] files = dirObj.listFiles();
//
//
//        for (File file : files) {
//            if (file.isDirectory()) {
//                String name = file.getName();
//
//                if (name.equals("Unlocked")) {
//                    addDirectoryToZip(parentDir, file, out, null);
//                    continue;
//                }
//
//                if (msetArrayList != null) {
//                    for (NemsFactory.Mset mset : msetArrayList) {
//                        if (mset.msetFile.getName().equals(name)) {
//                            addDirectoryToZip(parentDir, file, out, null);
//                            break;
//                        }
//                    }
//                } else {
//                    addDirectoryToZip(parentDir, file, out, null);
//                }
//
//                continue;
//            }
//
//            String name = file.getName();
//            if (!name.startsWith(sample) && name.endsWith(".properties")) {
//                continue; // other sample.properties are not added to the zip file
//            }
//
//            FileInputStream in = new FileInputStream(file.getAbsolutePath());
//
//            String relativePath = parentDir.toURI().relativize(file.toURI()).getPath();
//            ZipEntry zipEntry  = new ZipEntry(relativePath);
//
//            out.putNextEntry(zipEntry);
//            int len;
//            while ((len = in.read(tmpBuf)) > 0) {
//                out.write(tmpBuf, 0, len);
//            }
//            out.closeEntry();
//            in.close();
//        }
//    }
//    private static final byte[] tmpBuf = new byte[1024];

    @Override
    public String getFileName(){
        return zipFileName;
    }

    @Override
    public FileFilter getContentFilter() {
        return nemsFilter;
    }

    @Override
    public File[] getAssociatedFiles() {
        return new File[0];
    }

    @Override
    public String getAssociatedFileType(File associatedFile) {
        return null;
    }


    @Override
    public void setDataFormat(DataFormat format) {
        if(format instanceof NemsFormat)
            dataFormat=(NemsFormat) format;
    }

    static class NemsFilter implements FileFilter {

        /* (non-Javadoc)
         * @see java.io.FileFilter#accept(java.io.File)
         */
        public boolean accept(File pathname) {
            return true;
        }
    }
}
