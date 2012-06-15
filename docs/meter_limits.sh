inkscape --export-pdf=meter_storage.pdf meter_storage.svg
inkscape --export-pdf=meter_wait_limit.pdf meter_wait_limit.svg
inkscape --export-pdf=meter_target_range.pdf meter_target_range.svg
inkscape --export-pdf=meter_storage_fixed.pdf meter_storage_fixed.svg
inkscape --export-pdf=meter_wait_fixed.pdf meter_wait_fixed.svg
pdftk meter_storage.pdf meter_wait_limit.pdf meter_target_range.pdf meter_storage_fixed.pdf meter_wait_fixed.pdf cat output meter_limits.pdf 
