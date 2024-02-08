JC = javac
JC_FLAGS = -g:none

SRC_DIR = app/src/main/java/cashhub
BUILD_DIR = build
MAIN_CLASS = cashhub.App
JAR_NAME = cashhub.jar

SRC_FILES := $(shell find $(SRC_DIR) -type f -iname *.java)
CLASS_FILES := $(patsubst $(SRC_DIR)/%.java, $(BUILD_DIR)/%.class, $(SRC_FILES))

.PHONY: all
all: $(BUILD_DIR)
	$(JC) $(JC_FLAGS) -d $(BUILD_DIR) $(SRC_FILES)

$(BUILD_DIR):
	mkdir -p $(BUILD_DIR)

.PHONY: jar
jar: all
	jar cvfe $(JAR_NAME) $(MAIN_CLASS) -C $(BUILD_DIR) . 

.PHONY: run
run: all
	java -cp $(BUILD_DIR) $(MAIN_CLASS)

.PHONY: clean
clean:
	rm -rf $(BUILD_DIR)
	rm -f $(JAR_NAME)
