package com.lzs.jgit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.merge.ResolveMerger.MergeFailureReason;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
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

	public ExecGit(Git git) {
		this.git = git;
	}

	public ExecGit() {
		super();
	}

	public void showLog() throws GitAPIException {
		Iterable<RevCommit> revCommit = git.log().call();
		Iterator<RevCommit> iterator = revCommit.iterator();
		while (iterator.hasNext()) {
			System.out.println(iterator.next().getFullMessage() + "-----------");
		}
	}

	public void getBranch() throws GitAPIException {
		ListBranchCommand branchs = git.branchList();
		List<Ref> refs = branchs.call();
		for (Ref ref : refs) {
			System.out.println(ref.getName());
		}
	}

	public void commitRepository(String filepattern, String message) throws GitAPIException {
		AddCommand addCommand = git.add();
		addCommand.addFilepattern(filepattern).call();
		CommitCommand command = git.commit();
		command.setMessage(message);
		command.setAllowEmpty(true);
		PersonIdent author = new PersonIdent(aName, aEmailAddress);
		command.setAuthor(author);
		command.call();
	}

	public boolean pushRepository(String username, String password)
			throws InvalidRemoteException, TransportException, GitAPIException {
		boolean flag = false;
		PushCommand command = git.push();
		if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
			CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(username, password);
			command.setCredentialsProvider(credentialsProvider);
		}
		command.setAtomic(true).setForce(true);
		Iterable<PushResult> results = command.setPushAll().call();
		Iterator<PushResult> iterator = results.iterator();
		while (iterator.hasNext()) {
			Collection<RemoteRefUpdate> remoteUpdates = iterator.next().getRemoteUpdates();
			for (RemoteRefUpdate remoteRefUpdate : remoteUpdates) {
				if ("OK".equals(remoteRefUpdate.getStatus().name()))
					flag = true;
			}
		}
		return flag;
	}

	public boolean pushRepository() throws InvalidRemoteException, TransportException, GitAPIException {
		boolean flag = pushRepository(null, null);
		return flag;
	}

	public static ExecGit cloneRepository(String uri, String pathname) throws GitAPIException {
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
		PullResult result = command.call();
		command.setRebase(true).setRemoteBranchName("master");
		MergeResult mergeResult = result.getMergeResult();
		if (mergeResult != null) {
			Map<String, MergeFailureReason> failingPaths = mergeResult.getFailingPaths();
			if (failingPaths != null) {
				Set<Entry<String, MergeFailureReason>> entrySet = failingPaths.entrySet();
				for (Entry<String, MergeFailureReason> entry : entrySet) {
					System.out.println(entry.getKey() + "---" + entry.getValue().name());
				}
			}
			System.out.println("mergeStatus: " + mergeResult.getMergeStatus().isSuccessful());
		}
		boolean successful = result.isSuccessful();
		return successful;
	}

	public void removeBranch(String... names) throws GitAPIException {
		git.branchDelete().setBranchNames(names).call();
	}

	public void fetch(String username, String password)
			throws InvalidRemoteException, TransportException, GitAPIException {
		FetchCommand command = git.fetch();
		CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(username, password);
		command.setCredentialsProvider(credentialsProvider);
		FetchResult result = command.call();
		System.out.println(result.getMessages());
	}

	public void merge() throws GitAPIException {

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

	public static void main(String[] args) throws FileNotFoundException, IOException {
		try {
			//			ExecGit.cloneRepository("http://git.wex5.com:9999/lzs/lzs.git", "D:/tmp/test2");
			ExecGit git = new ExecGit("D:/tmp/test2");
			git.setaName("test");
			git.setaEmailAddress("test@163.com");
			git.getBranch();
						git.showLog();
//			git.commitRepository(".", "");
//			boolean flag = git.pullRepository("lzs", "1uzs.@00");
//			System.out.println(flag);
//			boolean pushResult = git.pushRepository("lzs", "1uzs.@00");
//			System.out.println(pushResult);
			//			System.out.println(git.listChangedFiles());
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
	}

}
