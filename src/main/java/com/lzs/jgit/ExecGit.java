package com.lzs.jgit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidMergeHeadsException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.BranchConfig.BranchRebaseMode;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import com.alibaba.fastjson.JSONObject;

public class ExecGit {

	private Git git;

	private String aName;

	private String aEmailAddress;

	public ExecGit(String pathname) throws FileNotFoundException, IOException {
		File file = new File(pathname + File.separator + ".git");
		if (!file.exists())
			throw new FileNotFoundException(file.getAbsolutePath() + "不存在");
		git = Git.open(file);
	}
	
	public ExecGit(Git git){
		this.git = git;
	}
	
	public ExecGit() {
		super();
	}

	public static void main(String[] args) {
		try {
			ExecGit git = new ExecGit("D:/tmp/test2");
//			git.fetch("lzs", "1uzs.@00");
			boolean flag = git.pullRepository("lzs", "1uzs.@00");
			System.out.println(flag);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
	}

	public void addIndex(String filepattern) throws NoFilepatternException, GitAPIException {
		AddCommand command = git.add();
		command.addFilepattern(filepattern).call();
	}

	public void commitRepository(String message) throws GitAPIException {
		CommitCommand command = git.commit();
		command.setMessage(message);
		command.setAllowEmpty(true);
		PersonIdent author = new PersonIdent(aName, aEmailAddress);
		command.setAuthor(author);
		command.call();
	}

	public void pushRepository() throws InvalidRemoteException, TransportException, GitAPIException {
		PushCommand command = git.push();
		command.setPushAll().call();
	}

	public static ExecGit cloneRepository(String uri,String pathname) throws GitAPIException {
		CloneCommand command = Git.cloneRepository();
		command.setURI(uri);
		CredentialsProvider provider = new UsernamePasswordCredentialsProvider("lzs", "1uzs.@00");
		command.setCredentialsProvider(provider);
		File file = new File(pathname);
		command.setDirectory(file);
		Git git = command.call();
		return new ExecGit(git);
	}

	public void diff() throws GitAPIException {
		DiffCommand command = git.diff();
		command.call();
	}

	/**
	 * <p>MethodName: pullRepository</p>
	 * <p>Description: git pull</p>
	 * @param username git的用户名
	 * @param password git的密码
	 * @return
	 * @throws GitAPIException
	 * @Date 2017年6月9日下午2:25:28
	 * @author luzs
	 */
	public boolean pullRepository(String username, String password) throws GitAPIException {
		PullCommand command = git.pull();
		if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
			CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(username, password);
			command.setCredentialsProvider(credentialsProvider);
		}
		command.setRebase(true);
		PullResult result = command.call();
		boolean successful = result.isSuccessful();
		return successful;
	}
	
	public void fetch(String username, String password) throws InvalidRemoteException, TransportException, GitAPIException{
		FetchCommand command = git.fetch();
		CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(username, password);
		command.setCredentialsProvider(credentialsProvider);
		FetchResult result = command.call();
		System.out.println(result.getMessages());
	}
	
	public void merge() throws GitAPIException{
		
//		git.pull().call();
			
	}
	
	public boolean pullRepository() throws GitAPIException {
		return this.pullRepository(null, null);
	}

	/**
	 * <p>MethodName: listChangedFiles</p>
	 * <p>Description: 获取修改的文件列表</p>
	 * @return
	 * @throws NoWorkTreeException
	 * @throws GitAPIException
	 * @Date 2017年6月9日下午2:11:48
	 * @author luzs
	 */
	public JSONObject listChangedFiles() throws NoWorkTreeException, GitAPIException {
		JSONObject result = new JSONObject();
		StatusCommand command = git.status();
		Status status = command.call();
		Set<String> untracked = status.getUntracked();//获取新增到workspace的文件列表
		Set<String> added = status.getAdded();//新添加到Index的文件列表，即untracked git add后的文件列表
		Set<String> changed = status.getChanged();//Index与HEAD有差异的文件列表
		Set<String> missing = status.getMissing();//workspace已删除，未放到Index的文件列表
		Set<String> modified = status.getModified();//workspace已修改，未添加到Index的文件列表
		Set<String> removed = status.getRemoved();//从Index移除的文件列表
		result.put("untracked", untracked);
		result.put("added", added);
		result.put("modified", modified);
		result.put("changed", changed);
		result.put("missing", missing);
		result.put("removed", removed);
		return result;
	}

	public boolean createFile(File file) {
		File parent = file.getParentFile();
		if (!parent.exists())
			if (!parent.mkdirs())
				return false;
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	public String getaName() {
		return aName;
	}

	public void setaName(String aName) {
		this.aName = aName;
	}

	public String getaEmailAddress() {
		return aEmailAddress;
	}

	public void setaEmailAddress(String aEmailAddress) {
		this.aEmailAddress = aEmailAddress;
	}

	public Git getGit() {
		return git;
	}

	public void setGit(Git git) {
		this.git = git;
	}

}
