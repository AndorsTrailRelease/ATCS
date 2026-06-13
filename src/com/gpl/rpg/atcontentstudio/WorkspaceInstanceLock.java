package com.gpl.rpg.atcontentstudio;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

public final class WorkspaceInstanceLock {

    private static final String LOCK_FILE_NAME = ".atcs.lock";

    private static WorkspaceLockHandle currentHandle = null;
    private static boolean shutdownHookRegistered = false;

    private WorkspaceInstanceLock() {
    }

    public static synchronized AcquireResult acquireForCurrentProcess(File workspaceRoot) {
        if (workspaceRoot == null) {
            return AcquireResult.error("Workspace path is missing.");
        }

        File normalizedWorkspaceRoot = workspaceRoot.getAbsoluteFile();
        if (currentHandle != null) {
            if (normalizedWorkspaceRoot.equals(currentHandle.workspaceRoot)) {
                return AcquireResult.acquired();
            }
            return AcquireResult.error("Another workspace is already active in this process.");
        }

        if (!normalizedWorkspaceRoot.exists() && !normalizedWorkspaceRoot.mkdirs()) {
            return AcquireResult.error("Unable to create workspace directory: " + normalizedWorkspaceRoot.getAbsolutePath());
        }
        if (!normalizedWorkspaceRoot.isDirectory()) {
            return AcquireResult.error("Workspace path is not a directory: " + normalizedWorkspaceRoot.getAbsolutePath());
        }

        File lockFile = new File(normalizedWorkspaceRoot, LOCK_FILE_NAME);
        RandomAccessFile lockAccess = null;
        FileChannel lockChannel = null;
        try {
            lockAccess = new RandomAccessFile(lockFile, "rw");
            lockChannel = lockAccess.getChannel();
            FileLock lock = tryLock(lockChannel);
            if (lock == null) {
                closeQuietly(lockChannel);
                closeQuietly(lockAccess);
                return AcquireResult.locked();
            }

            currentHandle = new WorkspaceLockHandle(normalizedWorkspaceRoot, lockAccess, lockChannel, lock);
            registerShutdownHook();
            return AcquireResult.acquired();
        } catch (IOException e) {
            closeQuietly(lockChannel);
            closeQuietly(lockAccess);
            return AcquireResult.error("Unable to lock workspace: " + e.getMessage());
        }
    }

    public static synchronized boolean isLockedByAnotherProcess(File workspaceRoot) {
        if (workspaceRoot == null) {
            return false;
        }

        File normalizedWorkspaceRoot = workspaceRoot.getAbsoluteFile();
        if (!normalizedWorkspaceRoot.exists() || !normalizedWorkspaceRoot.isDirectory()) {
            return false;
        }
        if (currentHandle != null && normalizedWorkspaceRoot.equals(currentHandle.workspaceRoot)) {
            return false;
        }

        File lockFile = new File(normalizedWorkspaceRoot, LOCK_FILE_NAME);
        if (!lockFile.exists()) {
            return false;
        }

        try (RandomAccessFile lockAccess = new RandomAccessFile(lockFile, "rw");
             FileChannel lockChannel = lockAccess.getChannel()) {
            FileLock lock = tryLock(lockChannel);
            if (lock == null) {
                return true;
            }
            lock.release();
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public static synchronized void releaseCurrentWorkspace() {
        if (currentHandle == null) {
            return;
        }

        closeQuietly(currentHandle.lock);
        closeQuietly(currentHandle.channel);
        closeQuietly(currentHandle.access);
        currentHandle = null;
    }

    private static FileLock tryLock(FileChannel channel) throws IOException {
        try {
            return channel.tryLock();
        } catch (OverlappingFileLockException e) {
            return null;
        }
    }

    private static void registerShutdownHook() {
        if (shutdownHookRegistered) {
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(WorkspaceInstanceLock::releaseCurrentWorkspace));
        shutdownHookRegistered = true;
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (Exception ignored) {
        }
    }

    private static final class WorkspaceLockHandle {
        private final File workspaceRoot;
        private final RandomAccessFile access;
        private final FileChannel channel;
        private final FileLock lock;

        private WorkspaceLockHandle(File workspaceRoot, RandomAccessFile access, FileChannel channel, FileLock lock) {
            this.workspaceRoot = workspaceRoot;
            this.access = access;
            this.channel = channel;
            this.lock = lock;
        }
    }

    public static final class AcquireResult {
        private final boolean acquired;
        private final boolean lockedByAnotherProcess;
        private final String errorMessage;

        private AcquireResult(boolean acquired, boolean lockedByAnotherProcess, String errorMessage) {
            this.acquired = acquired;
            this.lockedByAnotherProcess = lockedByAnotherProcess;
            this.errorMessage = errorMessage;
        }

        public static AcquireResult acquired() {
            return new AcquireResult(true, false, null);
        }

        public static AcquireResult locked() {
            return new AcquireResult(false, true, null);
        }

        public static AcquireResult error(String errorMessage) {
            return new AcquireResult(false, false, errorMessage);
        }

        public boolean isAcquired() {
            return acquired;
        }

        public boolean isLockedByAnotherProcess() {
            return lockedByAnotherProcess;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}

