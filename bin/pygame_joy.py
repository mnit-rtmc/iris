#!/bin/env python

import pygame
from sys import stdout

pygame.joystick.init()
joystick = pygame.joystick.Joystick(0)
joystick.init()
pygame.init()

while True:
	event = pygame.event.wait()
	if event.type == pygame.JOYAXISMOTION:
		print 'axis:%d,value:%f' % (event.axis, event.value)
	if event.type == pygame.JOYBUTTONUP:
		print 'button:%d,value:0' % (event.button)
	if event.type == pygame.JOYBUTTONDOWN:
		print 'button:%d,value:1' % (event.button)
	stdout.flush()
