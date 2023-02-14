\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Fix msg_line hashtags
UPDATE iris.msg_line SET msg_pattern = '.2.LINE'
    WHERE msg_pattern = '.3.LINE' AND restrict_hashtag IN
(
    SELECT '#' || dms FROM dms_hashtag_view WHERE hashtag = '#TwoLine'
);

UPDATE iris.msg_line SET msg_pattern = '.3.LINE'
    WHERE msg_pattern = '.2.PAGE' AND restrict_hashtag IN
(
    SELECT '#' || dms FROM dms_hashtag_view WHERE hashtag = '#ThreeLine'
);

COMMIT;
