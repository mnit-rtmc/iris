/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2011  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/wait.h>
#include <errno.h>
#include <signal.h>
/*
 * This is a helper program, designed to start a (Java) service. If the program
 * dies unexpectedly, it will be restarted. If this program is killed, it will
 * stop the java service as well.
 */

/* Child process ID */
int pid;

/* Signal handle for SIGTERM.  This cleans up by killing the child process
 * before exiting. */
static void handle_signal(int sig_num) {
	if(pid > 0)
		kill(pid, sig_num);
	exit(1);
}

/* Exec the child process */
static int exec_child(char **args) {
	signal(SIGTERM, handle_signal);
	while(1) {
		pid = fork();
		if(pid < 0)
			return 1;
		else if(pid > 0) {
			int status;
			if(wait(&status) < 0)
				return 1;
			if(WIFEXITED(status) && 
			  (WEXITSTATUS(status) == EXIT_SUCCESS))
				return 0;
		} else {
			execv(args[0], args);
			/* exec only returns on errors ... */
			return 1;
		}
	}
}

/* Main entry point */
int main(int argc, char *args[]) {
	if(argc < 2) {
		fprintf(stderr, "Syntax: %s <file> [arg1] ... [argN]\n",
			args[0]);
		return 1;
	}
	memmove(args, args + 1, argc * sizeof(char *));
	args[argc] = NULL;
	if(exec_child(args)) {
		fprintf(stderr, "%s\n", strerror(errno));
		return 1;
	} else
		return 0;
}
