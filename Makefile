JC = javac
JC_FLAGS = -g:none

SRC_DIR = app/src/main/java/cashhub
BUILD_DIR = build
MAIN_CLASS = cashhub.App

SRC_FILES := $(shell find $(SRC_DIR) -type f -iname *.java)

.PHONY: all
all: $(BUILD_DIR)
	$(JC) $(JC_FLAGS) -d $(BUILD_DIR) $(SRC_FILES)

$(BUILD_DIR):
	mkdir -p $(BUILD_DIR)

.PHONY: run
run: all
	java -cp $(BUILD_DIR) $(MAIN_CLASS)

.PHONY: clean
clean:
	rm -r $(BUILD_DIR)
