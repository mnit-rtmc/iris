wkhtmltopdf -s Letter density_metering.html meter_text.pdf
inkscape --export-pdf=meter_undercount.pdf meter_undercount.svg
inkscape --export-pdf=meter_storage.pdf meter_storage.svg
inkscape --export-pdf=meter_wait_limit.pdf meter_wait_limit.svg
inkscape --export-pdf=meter_target_range.pdf meter_target_range.svg
inkscape --export-pdf=meter_storage_fixed.pdf meter_storage_fixed.svg
inkscape --export-pdf=meter_wait_fixed.pdf meter_wait_fixed.svg
pdftk meter_text.pdf meter_undercount.pdf meter_storage.pdf meter_wait_limit.pdf meter_target_range.pdf meter_storage_fixed.pdf meter_wait_fixed.pdf cat output density_metering.pdf
