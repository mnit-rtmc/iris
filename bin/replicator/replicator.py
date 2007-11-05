#!/usr/bin/env python

from os import chdir, listdir, makedirs, rmdir, unlink, symlink, system
from os.path import basename, dirname, isdir, isfile, islink, join
from ftplib import FTP

class ConfigError(Exception):
	pass

def remove_symlinks(path):
	# Remove all symlinks (and empty directories) from the specified path
	rem = 0
	for f in listdir(path):
		p = join(path, f)
		if isdir(p):
			rem += remove_symlinks(p)
		elif islink(p):
			unlink(p)
		else:
			rem += 1
	if rem == 0:
		rmdir(path)
	return rem

def read_configuration(fname):
	params = {}
	for line in open(fname):
		args = line.split()
		host = args.pop(0)
		transport = args.pop(0)
		params[host] = (transport, ' '.join(args))
	return params

class Replicator(object):

	def __init__(self, store):
		self.store = store
		self.params = read_configuration(join(self.store, '.replica'))

	def copy_to_host(self, host, path, rpath=None):
		if not isfile(path):
			raise ConfigError, 'File not found: %s' % path
		if rpath is None:
			rpath = dirname(path)
		if host in self.params:
			hname = join(self.store, host)
			lname = join(hname, rpath)
			if not isdir(lname):
				makedirs(lname)
			fname = join(lname, basename(path))
			print path, fname
			symlink(path, fname)
		else:
			raise ConfigError, 'Unknown host: %s' % host

	def replicate_ftp_dir(self, ftp, path, rpath):
		rem = 0
		for f in listdir(path):
			fname = join(path, f)
			rname = join(rpath, f)
			if isdir(fname):
				rem += self.replicate_ftp_dir(ftp, fname, rname)
			elif islink(fname):
				ftp.storbinary('STOR %s' % rname, open(fname))
				unlink(fname)
			else:
				rem += 1
		if rem == 0:
			rmdir(path)
		return rem

	def replicate_ftp(self, host, path, args):
		user, password = args.split()
		ftp = FTP(host, user, password)
		self.replicate_ftp_dir(ftp, path, '')
		ftp.quit()

	def replicate_scp(self, host, path, args):
		chdir(path)
		cmnd = '/usr/bin/scp %s . %s:' % (args, host)
		if system(cmnd):
			raise SystemError
		remove_symlinks(path)

	def replicate_to_host(self, host, path):
		if host in self.params:
			transport, args = self.params[host]
			if transport == 'ftp':
				self.replicate_ftp(host, path, args)
			elif transport == 'scp':
				self.replicate_scp(host, path, args)
			else:
				raise ConfigError, \
					'Unknown transport: %s' % transport
		else:
			raise ConfigError, 'Unknown host: %s' % host

	def replicate(self):
		for host in listdir(self.store):
			path = join(self.store, host)
			if isdir(path):
				self.replicate_to_host(host, path)

r = Replicator('/var/local/clone/')
r.copy_to_host('data.dot.state.mn.us', '/home/lau1dou/I394.csv', 'tmcdata')
#r.replicate()
