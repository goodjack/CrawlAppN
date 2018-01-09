package com.eopcon.crawler.samsungcnt.common.file.service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.eopcon.crawler.samsungcnt.common.file.LocalFile;
import com.eopcon.crawler.samsungcnt.common.file.LocalFileSet;

public class LocalFileService {

	public static final String DEFAULT_FILE_RENAME_EXPRESSION = "#name.replaceAll('^(.+)\\.(\\w+)$', '$1-' + #today + '.$2')";

	public void mv(String basePath, List<LocalFile> lfs, String expression) {

		ExpressionParser parser = new SpelExpressionParser();
		Expression exp = parser.parseExpression(expression);

		StandardEvaluationContext context = new StandardEvaluationContext();

		String today = new SimpleDateFormat("yyyyMMdd").format(new Date());

		for (LocalFile lf : lfs) {
			if (!lf.isDelete()) {
				String fileName = lf.getFileName();
				File file = lf.getFile();

				context.setVariable("name", fileName);
				context.setVariable("today", today);

				String newFileName = exp.getValue(context, String.class);
				File target = new File(basePath + "/" + newFileName);

				if (target.exists())
					target.delete();

				file.renameTo(target);
			}
		}
	}

	public void rm(List<LocalFile> lfs) {
		for (LocalFile lf : lfs) {
			if (!lf.isDelete()) {
				File file = lf.getFile();
				lf.setDelete(file.delete());
			}
		}
	}

	public LocalFileSet ls(String basePath, boolean recursive) {
		FileTraverse ft = new FileTraverse(basePath, recursive);
		ft.search();

		return new LocalFileSet(ft.getDirs(), ft.getFiles());
	}

	private class FileTraverse {
		private List<String> dirs = new ArrayList<String>();
		private List<LocalFile> files = new ArrayList<LocalFile>();

		private String basePath;
		private boolean recursive;

		private final String BACKSLASH = "\\\\";
		private final String SLASH = "/";

		FileTraverse(String basePath, boolean recursive) {
			this.basePath = basePath.replaceAll(BACKSLASH, SLASH);
			this.recursive = recursive;
		}

		void search() {
			traverse(new File(basePath));
		}

		List<String> getDirs() {
			return dirs;
		}

		List<LocalFile> getFiles() {
			return files;
		}

		private void traverse(File file) {
			if (file.isDirectory()) {
				addDirectory(file);

				File[] childs = file.listFiles();

				for (int i = 0; i < childs.length; i++) {
					if (recursive && childs[i].isDirectory()) {
						traverse(childs[i]);
					} else {
						if (!childs[i].isDirectory())
							addFile(childs[i]);
					}
				}
			} else {
				addFile(file);
			}
		}

		private void addFile(File file) {

			String relativePath = file.getParent().replaceAll(BACKSLASH, SLASH).replace(basePath, "");
			String fileName = file.getName();

			LocalFile localFile = new LocalFile();

			localFile.setRelativePath(relativePath);
			localFile.setFileName(fileName);
			localFile.setFile(file);

			files.add(localFile);
		}

		private void addDirectory(File dir) {
			String relativePath = dir.getAbsolutePath().replaceAll(BACKSLASH, SLASH).replace(basePath, "");
			if (relativePath.length() > 0)
				dirs.add(relativePath);
		}
	}
}
