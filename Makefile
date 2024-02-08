JC = javac
JC_FLAGS = -g:none

SRC_DIR = app/src/main/java/cashhub
BUILD_DIR = build
MAIN_CLASS = cashhub.App
WORK_DIR = app
JAR_NAME = cashhub.jar

FULL_BUILD_DIR = $(WORK_DIR)/$(BUILD_DIR)

SRC_FILES := $(shell find $(SRC_DIR) -type f -iname *.java)
CLASS_FILES := $(patsubst $(SRC_DIR)/%.java, $(FULL_BUILD_DIR)/%.class, $(SRC_FILES))

.PHONY: all
all: $(FULL_BUILD_DIR)
	$(JC) $(JC_FLAGS) -d $(FULL_BUILD_DIR) $(SRC_FILES)

$(FULL_BUILD_DIR):
	mkdir -p $(FULL_BUILD_DIR)

.PHONY: jar
jar: all
	jar cvfe $(JAR_NAME) $(MAIN_CLASS) -C $(FULL_BUILD_DIR) . 

.PHONY: run
run: all
	cd $(WORK_DIR) && java -cp $(BUILD_DIR) $(MAIN_CLASS)

.PHONY: clean
clean:
	rm -rf $(FULL_BUILD_DIR)
	rm -f $(JAR_NAME)
